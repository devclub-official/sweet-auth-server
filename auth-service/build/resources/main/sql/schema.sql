create table if not exists spring.users (
    `id` int not null auto_increment,
    `user_id` varchar(45) not null,
    `password` text not null,
    primary key (`id`)
);

create table if not exists spring.otp (
    `id` int not null auto_increment,
    `user_id` varchar(45) not null,
    `otp_code` varchar(45) not null,
    primary key (`id`)
);