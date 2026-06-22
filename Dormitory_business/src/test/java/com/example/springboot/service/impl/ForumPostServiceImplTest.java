package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.ForumComment;
import com.example.springboot.entity.ForumPost;
import com.example.springboot.entity.Student;
import com.example.springboot.mapper.ForumCommentMapper;
import com.example.springboot.mapper.ForumPostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 帖子管理模块 — Service 层单元测试
 * 覆盖 addPost / find / detail / updatePost / deletePost
 */
@DisplayName("帖子管理 Service 层测试")
class ForumPostServiceImplTest {

    /* ==================== 添加帖子 ==================== */

    @Nested
    @DisplayName("添加帖子 — addPost")
    class AddPost {

        @Test
        @DisplayName("学生发帖成功：应使用 Session 中的学生身份作为作者")
        void shouldUseSessionStudentAsAuthor() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            ForumPost post = new ForumPost();
            post.setTitle("寻找丢失的雨伞");
            post.setContent("蓝色雨伞，在3号楼附近遗失");
            post.setCategory("失物");
            post.setAuthorUsername("hacker");  // 试图伪造作者
            post.setAuthorName("Fake");

            when(postMapper.insert(any(ForumPost.class))).thenReturn(1);

            int result = service.addPost(post, studentSession("S001", "张三"));

            assertThat(result).isEqualTo(1);
            verify(postMapper).insert(org.mockito.ArgumentMatchers.argThat(saved ->
                    "S001".equals(saved.getAuthorUsername())
                            && "张三".equals(saved.getAuthorName())
                            && saved.getDeleted() == 0
                            && saved.getCreateTime() != null
                            && saved.getUpdateTime() != null));
        }

        @Test
        @DisplayName("发帖被拒：非学生身份（宿管/管理员）不能发帖")
        void shouldRejectNonStudentIdentity() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            ForumPost post = new ForumPost();
            post.setTitle("管理员通知");
            post.setContent("明天检查卫生");
            post.setCategory("分享");

            int result = service.addPost(post, managerSession());

            assertThat(result).isEqualTo(0);
            verify(postMapper, never()).insert(any(ForumPost.class));
        }

        @Test
        @DisplayName("发帖被拒：非法分类（不在允许列表中）")
        void shouldRejectUnknownCategory() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            ForumPost post = new ForumPost();
            post.setTitle("测试帖子");
            post.setContent("测试内容");
            post.setCategory("其他");  // 不在 "失物/招领/分享/求助" 中

            int result = service.addPost(post, studentSession("S001", "张三"));

            assertThat(result).isEqualTo(0);
            verify(postMapper, never()).insert(any(ForumPost.class));
        }

        @Test
        @DisplayName("发帖被拒：标题为空")
        void shouldRejectEmptyTitle() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            ForumPost post = new ForumPost();
            post.setTitle("");
            post.setContent("测试内容");
            post.setCategory("分享");

            int result = service.addPost(post, studentSession("S001", "张三"));

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("发帖被拒：内容为空")
        void shouldRejectEmptyContent() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            ForumPost post = new ForumPost();
            post.setTitle("测试标题");
            post.setContent("   ");  // 空白字符串
            post.setCategory("分享");

            int result = service.addPost(post, studentSession("S001", "张三"));

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("图片名包含路径遍历字符应被拒绝")
        void shouldRejectImageWithPathTraversal() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            ForumPost post = new ForumPost();
            post.setTitle("带图片帖子");
            post.setContent("内容");
            post.setCategory("分享");
            post.setImage("../../../etc/passwd");  // 路径遍历攻击

            int result = service.addPost(post, studentSession("S001", "张三"));

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("合法图片名可以正常发帖")
        void shouldAcceptValidImageName() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            ForumPost post = new ForumPost();
            post.setTitle("带图片帖子");
            post.setContent("内容");
            post.setCategory("分享");
            post.setImage("photo_2024.jpg");  // 合法文件名

            when(postMapper.insert(any(ForumPost.class))).thenReturn(1);

            int result = service.addPost(post, studentSession("S001", "张三"));

            assertThat(result).isEqualTo(1);
        }
    }

    /* ==================== 分页查询帖子 ==================== */

    @Nested
    @DisplayName("分页查询帖子 — find")
    class Find {

        @Test
        @DisplayName("无条件查询：返回未删除的帖子分页数据（按时间倒序）")
        void shouldReturnNonDeletedPostsOrderedByTime() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            Page<ForumPost> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(
                    buildPost(1, "帖子1", "失物"),
                    buildPost(2, "帖子2", "分享")
            ));
            mockPage.setTotal(2);
            when(postMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                    .thenReturn(mockPage);

            Page result = service.find(1, 10, null, null);

            assertThat(result.getRecords()).hasSize(2);
            assertThat(result.getTotal()).isEqualTo(2);
        }

        @Test
        @DisplayName("按分类筛选：仅返回指定分类的帖子")
        void shouldFilterByCategory() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            Page<ForumPost> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.singletonList(buildPost(1, "丢失物", "失物")));
            mockPage.setTotal(1);
            when(postMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                    .thenReturn(mockPage);

            Page result = service.find(1, 10, null, "失物");

            assertThat(result.getRecords()).hasSize(1);
        }

        @Test
        @DisplayName("按关键词搜索：匹配标题或内容")
        void shouldSearchByTitleOrContent() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            Page<ForumPost> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.singletonList(buildPost(1, "关于雨伞", "失物")));
            mockPage.setTotal(1);
            when(postMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                    .thenReturn(mockPage);

            Page result = service.find(1, 10, "雨伞", null);

            assertThat(result.getRecords()).hasSize(1);
        }
    }

    /* ==================== 帖子详情 ==================== */

    @Nested
    @DisplayName("帖子详情 — detail")
    class Detail {

        @Test
        @DisplayName("查询成功：返回帖子及其评论列表")
        void shouldReturnPostWithComments() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);
            ForumCommentMapper commentMapper = commentMapper(service);

            ForumPost post = buildPost(1, "测试帖子", "分享");
            when(postMapper.selectById(1)).thenReturn(post);

            ForumComment comment = new ForumComment();
            comment.setId(1);
            comment.setPostId(1);
            comment.setContent("回复内容");
            comment.setDeleted(0);
            when(commentMapper.selectList(any(QueryWrapper.class)))
                    .thenReturn(Collections.singletonList(comment));

            Map<String, Object> result = service.detail(1);

            assertThat(result).isNotNull();
            assertThat(result.get("post")).isEqualTo(post);
            assertThat((java.util.List<?>) result.get("comments")).hasSize(1);
        }

        @Test
        @DisplayName("帖子已被删除：应返回 null")
        void shouldReturnNullWhenPostDeleted() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            ForumPost post = buildPost(1, "已删除帖子", "分享");
            post.setDeleted(1);
            when(postMapper.selectById(1)).thenReturn(post);

            Map<String, Object> result = service.detail(1);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("帖子不存在：应返回 null")
        void shouldReturnNullWhenPostNotFound() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            when(postMapper.selectById(9999)).thenReturn(null);

            Map<String, Object> result = service.detail(9999);

            assertThat(result).isNull();
        }
    }

    /* ==================== 更新帖子 ==================== */

    @Nested
    @DisplayName("更新帖子 — updatePost")
    class UpdatePost {

        @Test
        @DisplayName("作者本人更新成功：应修改帖子内容并更新时间")
        void shouldAllowAuthorToUpdate() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            ForumPost current = buildPost(1, "原标题", "求助");
            current.setAuthorUsername("S001");
            when(postMapper.selectById(1)).thenReturn(current);
            when(postMapper.updateById(any(ForumPost.class))).thenReturn(1);

            ForumPost update = new ForumPost();
            update.setId(1);
            update.setTitle("修改后的标题");
            update.setContent("修改后的内容");
            update.setCategory("分享");

            int result = service.updatePost(update, studentSession("S001", "张三"));

            assertThat(result).isEqualTo(1);
            verify(postMapper).updateById(org.mockito.ArgumentMatchers.argThat(saved ->
                    "修改后的标题".equals(saved.getTitle())
                            && "修改后的内容".equals(saved.getContent())
                            && "分享".equals(saved.getCategory())));
        }

        @Test
        @DisplayName("非作者更新被拒：其他学生不能修改别人的帖子")
        void shouldRejectNonAuthorUpdate() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            ForumPost current = buildPost(1, "原标题", "求助");
            current.setAuthorUsername("S001");
            when(postMapper.selectById(1)).thenReturn(current);

            ForumPost update = new ForumPost();
            update.setId(1);
            update.setTitle("篡改标题");
            update.setContent("篡改内容");
            update.setCategory("失物");

            int result = service.updatePost(update, studentSession("S002", "李四"));

            assertThat(result).isEqualTo(0);
            verify(postMapper, never()).updateById(any(ForumPost.class));
        }

        @Test
        @DisplayName("更新被拒：帖子已被删除")
        void shouldRejectUpdateDeletedPost() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            ForumPost current = buildPost(1, "已删除", "求助");
            current.setAuthorUsername("S001");
            current.setDeleted(1);
            when(postMapper.selectById(1)).thenReturn(current);

            ForumPost update = new ForumPost();
            update.setId(1);
            update.setTitle("新标题");
            update.setContent("新内容");
            update.setCategory("分享");

            int result = service.updatePost(update, studentSession("S001", "张三"));

            assertThat(result).isEqualTo(0);
        }
    }

    /* ==================== 删除帖子（软删除） ==================== */

    @Nested
    @DisplayName("删除帖子 — deletePost")
    class DeletePost {

        @Test
        @DisplayName("作者可以删除自己的帖子")
        void shouldAllowAuthorToDelete() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);
            ForumCommentMapper commentMapper = commentMapper(service);

            ForumPost post = buildPost(1, "我的帖子", "分享");
            post.setAuthorUsername("S001");
            when(postMapper.selectById(1)).thenReturn(post);
            when(postMapper.updateById(any(ForumPost.class))).thenReturn(1);
            when(commentMapper.update(any(ForumComment.class), any(Wrapper.class)))
                    .thenReturn(1);

            int result = service.deletePost(1, studentSession("S001", "张三"));

            assertThat(result).isEqualTo(1);
            verify(postMapper).updateById(org.mockito.ArgumentMatchers.argThat(saved ->
                    saved.getDeleted() == 1));
        }

        @Test
        @DisplayName("管理员可以删除任何帖子")
        void shouldAllowAdminToDelete() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);
            ForumCommentMapper commentMapper = commentMapper(service);

            ForumPost post = buildPost(1, "违规帖子", "分享");
            post.setAuthorUsername("S001");
            when(postMapper.selectById(1)).thenReturn(post);
            when(postMapper.updateById(any(ForumPost.class))).thenReturn(1);
            when(commentMapper.update(any(ForumComment.class), any(Wrapper.class)))
                    .thenReturn(1);

            int result = service.deletePost(1, adminSession());

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("宿管可以删除管辖楼宇内的帖子")
        void shouldAllowDormManagerToDelete() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);
            ForumCommentMapper commentMapper = commentMapper(service);

            ForumPost post = buildPost(1, "问题帖子", "求助");
            post.setAuthorUsername("S001");
            when(postMapper.selectById(1)).thenReturn(post);
            when(postMapper.updateById(any(ForumPost.class))).thenReturn(1);
            when(commentMapper.update(any(ForumComment.class), any(Wrapper.class)))
                    .thenReturn(1);

            int result = service.deletePost(1, dormManagerSession());

            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("非作者学生不能删除别人的帖子")
        void shouldRejectNonOwnerStudentDelete() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            ForumPost post = buildPost(1, "别人的帖子", "分享");
            post.setAuthorUsername("S001");
            when(postMapper.selectById(1)).thenReturn(post);

            int result = service.deletePost(1, studentSession("S002", "李四"));

            assertThat(result).isEqualTo(0);
            verify(postMapper, never()).updateById(any(ForumPost.class));
        }

        @Test
        @DisplayName("删除已删除的帖子应返回 0")
        void shouldRejectDeleteAlreadyDeletedPost() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);

            ForumPost post = buildPost(1, "已删除", "分享");
            post.setAuthorUsername("S001");
            post.setDeleted(1);
            when(postMapper.selectById(1)).thenReturn(post);

            int result = service.deletePost(1, studentSession("S001", "张三"));

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("删除帖子时级联软删除关联评论")
        void shouldCascadeDeleteComments() {
            ForumPostServiceImpl service = newService();
            ForumPostMapper postMapper = mapper(service);
            ForumCommentMapper commentMapper = commentMapper(service);

            ForumPost post = buildPost(1, "测试帖", "分享");
            post.setAuthorUsername("S001");
            when(postMapper.selectById(1)).thenReturn(post);
            when(postMapper.updateById(any(ForumPost.class))).thenReturn(1);
            when(commentMapper.update(any(ForumComment.class), any(Wrapper.class)))
                    .thenReturn(3);  // 级联删除 3 条评论

            int result = service.deletePost(1, studentSession("S001", "张三"));

            assertThat(result).isEqualTo(1);
            verify(commentMapper).update(any(ForumComment.class), any(Wrapper.class));
        }
    }

    /* ==================== 辅助方法 ==================== */

    private ForumPostServiceImpl newService() {
        ForumPostServiceImpl service = new ForumPostServiceImpl();
        ReflectionTestUtils.setField(service, "forumPostMapper", mock(ForumPostMapper.class));
        ReflectionTestUtils.setField(service, "forumCommentMapper", mock(ForumCommentMapper.class));
        return service;
    }

    @SuppressWarnings("unchecked")
    private ForumPostMapper mapper(Object service) {
        return (ForumPostMapper) ReflectionTestUtils.getField(service, "forumPostMapper");
    }

    @SuppressWarnings("unchecked")
    private ForumCommentMapper commentMapper(Object service) {
        return (ForumCommentMapper) ReflectionTestUtils.getField(service, "forumCommentMapper");
    }

    private ForumPost buildPost(Integer id, String title, String category) {
        ForumPost post = new ForumPost();
        post.setId(id);
        post.setTitle(title);
        post.setContent("正文内容");
        post.setCategory(category);
        post.setAuthorUsername("S001");
        post.setAuthorName("张三");
        post.setCreateTime("2024-01-15 10:00:00");
        post.setUpdateTime("2024-01-15 10:00:00");
        post.setDeleted(0);
        return post;
    }

    private MockHttpSession studentSession(String username, String name) {
        Student student = new Student();
        student.setUsername(username);
        student.setName(name);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("Identity", "stu");
        session.setAttribute("User", student);
        return session;
    }

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("Identity", "admin");
        session.setAttribute("User", new Object());
        return session;
    }

    private MockHttpSession dormManagerSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("Identity", "dormManager");
        session.setAttribute("User", new Object());
        return session;
    }

    private MockHttpSession managerSession() {
        return dormManagerSession();
    }
}
