CREATE TABLE if not exists user (
  id          bigint      NOT NULL AUTO_INCREMENT,
  username    varchar(50) NOT NULL,
  password    char(60)    NOT NULL,
  first_name  varchar(20) NOT NULL,
  date_joined datetime    NOT NULL DEFAULT current_timestamp,
  last_login  datetime    NOT NULL DEFAULT current_timestamp,
  PRIMARY KEY (id),
  UNIQUE KEY username (username)
);

/* product_number varchar(255) null key=mul */
/* products id int(11) primary key auto_increment,
product_number varchar(255) uni key */


CREATE TABLE if not exists post (
  id            bigint       NOT NULL AUTO_INCREMENT,
  title         varchar(255) NOT NULL,
  content       text         NOT NULL,
  user_id       bigint       NOT NULL, -- references user.id
  pub_date      datetime     NOT NULL DEFAULT current_timestamp(),
  last_modified timestamp    NOT NULL DEFAULT current_timestamp() on update current_timestamp(),
  PRIMARY KEY (id),
  FULLTEXT KEY title (title),
  FULLTEXT KEY content (content),
  FOREIGN KEY (user_id) REFERENCES user (id)
);