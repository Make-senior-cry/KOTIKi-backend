create table usr
(
    id bigserial primary key,
    name varchar(64) not null,
    email varchar(50) not null,
    password_hash varchar(60) not null,
    image_path varchar(260),
    description varchar(2048),
    role varchar(32) not null,
    creation_timestamp timestamp not null
);

create table post
(
    id bigserial primary key,
    text varchar(2048),
    image_path varchar(260),
    creation_timestamp timestamp not null,
    is_banned boolean not null default false,
    author_id bigint not null,
    foreign key (author_id) references usr(id)
);

create table tag
(
    id bigserial primary key,
    name varchar(32) not null
);

create table user_tag
(
    user_id bigint not null primary key,
    tag_id  bigint not null,
    foreign key (user_id) references usr(id),
    foreign key (tag_id) references tag(id)
);

create table post_tag
(
    post_id bigint not null,
    tag_id  bigint not null,
    foreign key (post_id) references post(id),
    foreign key (tag_id) references tag(id)
);

create table user_user
(
    follower_id  bigint not null,
    following_id bigint not null,
    foreign key (follower_id) references usr(id),
    foreign key (following_id) references usr(id)
);

create table chat_room
(
    id bigserial primary key,
    user1_id bigint not null,
    user2_id bigint not null,
    foreign key (user1_id) references usr(id),
    foreign key (user2_id) references usr(id)
);

create table chat_message
(
    id bigserial primary key,
    text varchar(1024) not null,
    send_timestamp timestamp not null,
    sender_id bigint not null,
    receiver_id bigint not null,
    checked bool default false,
    foreign key (sender_id) references usr(id),
    foreign key (receiver_id) references usr(id)
);

create table post_like
(
    id bigserial primary key,
    post_id bigint not null,
    user_id bigint not null,
    foreign key (post_id) references post(id),
    foreign key (user_id) references usr(id)
);

create table post_report
(
    id bigserial primary key,
    post_id bigint not null,
    user_id bigint not null,
    foreign key (post_id) references post(id),
    foreign key (user_id) references usr(id)
);
