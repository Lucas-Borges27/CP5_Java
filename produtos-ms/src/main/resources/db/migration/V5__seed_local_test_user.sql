insert ignore into usuario(login, senha)
values ('user@duckbiil.com', '{noop}123');

insert ignore into usuarios_roles(login, role)
values ('user@duckbiil.com', 'ROLE_USER');
