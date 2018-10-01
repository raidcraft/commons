-- apply changes
create table rc_player_placed_blocks (
  id                            integer auto_increment not null,
  world                         varchar(40),
  chunk_x                       integer not null,
  chunk_z                       integer not null,
  x                             integer not null,
  y                             integer not null,
  z                             integer not null,
  timestamp                     datetime(6),
  constraint pk_rc_player_placed_blocks primary key (id)
);

create table rc_actionapi (
  id                            integer auto_increment not null,
  action_type                   varchar(255),
  name                          varchar(255),
  description                   varchar(255),
  server                        varchar(255),
  conf                          longtext,
  active                        tinyint(1) default 0 not null,
  last_active                   datetime(6),
  constraint pk_rc_actionapi primary key (id)
);

create table rc_commands (
  id                            integer auto_increment not null,
  host                          varchar(255),
  base                          varchar(255),
  aliases                       varchar(255),
  description                   varchar(255),
  usage_                        varchar(255),
  min                           integer not null,
  max                           integer not null,
  flags                         varchar(255),
  help_                         TEXT,
  permission                    varchar(255),
  server                        varchar(255),
  constraint pk_rc_commands primary key (id)
);

create table rc_listener (
  id                            integer auto_increment not null,
  listener                      varchar(255),
  plugin                        varchar(255),
  last_loaded                   datetime(6),
  server                        varchar(255),
  constraint uq_rc_listener_listener_server unique (listener,server),
  constraint pk_rc_listener primary key (id)
);

create table rc_player_logs (
  id                            integer auto_increment not null,
  player                        varchar(40),
  name                          varchar(255),
  join_time                     datetime(6),
  quit_time                     datetime(6),
  world                         varchar(255),
  constraint pk_rc_player_logs primary key (id)
);

create table rc_player_log_stats (
  id                            integer auto_increment not null,
  log_id                        integer,
  statistic                     varchar(255),
  logon_value                   integer not null,
  logoff_value                  integer not null,
  constraint pk_rc_player_log_stats primary key (id)
);

create table rc_plugin (
  id                            integer auto_increment not null,
  name                          varchar(255),
  description                   varchar(255),
  version                       varchar(255),
  author                        varchar(255),
  last_active                   datetime(6),
  constraint pk_rc_plugin primary key (id)
);

create table rc_players (
  id                            integer auto_increment not null,
  uuid                          varchar(40),
  last_name                     varchar(255),
  last_joined                   datetime(6),
  first_joined                  datetime(6),
  constraint pk_rc_players primary key (id)
);

create index ix_rc_player_log_stats_log_id on rc_player_log_stats (log_id);
alter table rc_player_log_stats add constraint fk_rc_player_log_stats_log_id foreign key (log_id) references rc_player_logs (id) on delete restrict on update restrict;

