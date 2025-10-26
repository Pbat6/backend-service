-- Chèn các vai trò
insert into tbl_role (id, name) values (1, 'ADMIN'), (2, 'MANAGER'), (3, 'USER') on conflict (id) do nothing;

-- Chèn các quyền hạn mới
insert into tbl_permission (id, name, description) values
                                                       (1, 'user:create', 'Create a new user'),
                                                       (2, 'user:view', 'View user information'),
                                                       (3, 'user:update', 'Update user information'),
                                                       (4, 'user:delete', 'Delete a user'),
                                                       (5, 'user:resend_link', 'Resend activation/reset link'),
                                                       (6, 'course:create', 'Create a new course'),
                                                       (7, 'course:view', 'View course information'),
                                                       (8, 'course:update', 'Update course information'),
                                                       (9, 'course:delete', 'Delete a course'),
                                                       (10, 'course:enroll', 'Enroll in a course')

-- Gán quyền cho ADMIN (có tất cả quyền)
insert into tbl_role_has_permission (role_id, permission_id) values
                                                                 (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10)

-- Gán quyền cho MANAGER
insert into tbl_role_has_permission (role_id, permission_id) values
                                                                 (2, 1), -- user:create
                                                                 (2, 2), -- user:view
                                                                 (2, 4), -- user:delete
                                                                 (2, 5), -- user:resend_link
                                                                 (2, 6), -- course:create
                                                                 (2, 7), -- course:view
                                                                 (2, 8)  -- course:update

-- Gán quyền cho USER (chỉ có các quyền cơ bản)
insert into tbl_role_has_permission (role_id, permission_id) values
                                                                 (3, 7), -- course:view
                                                                 (3, 10) -- course:enroll

insert into tbl_user (username, password, status )
values ('admin','$2a$12$iaFviHXffwJZt0M8uqUUQ.DN6RJ3/0jDgYrToS58U4Vtpu9cU/TDe', 'ACTIVE');

insert into tbl_user_has_role (role_id, user_id)
values (1, 1);