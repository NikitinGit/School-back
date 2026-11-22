-- Тестовые учётные записи: student / Student123, teacher / Teacher123.
-- Пароли хранятся в формате Spring Security DelegatingPasswordEncoder (bcrypt).
INSERT INTO users (login, password, role, first_name, second_name, email, birth_date)
VALUES
    ('student', '{bcrypt}$2a$10$UQSSF8wQlGKLXOwfgs5bKeeo7VxtBEhEbcR5a/toYu7eAmrmu5Vfi',
     'STUDENT', 'Иван', 'Учеников', 'student@example.com', DATE '2010-05-15'),
    ('teacher', '{bcrypt}$2a$10$4G7hGKiplvlxbrs6BNVtyuW5p8CSLwC4vqoL/amgtdXqLgB8p63Wm',
     'TEACHER', 'Анна', 'Учителева', 'teacher@example.com', DATE '1990-03-20')
ON CONFLICT (login) DO NOTHING;
