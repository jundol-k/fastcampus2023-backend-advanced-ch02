package com.example.webflux1.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Repository
@RequiredArgsConstructor
public class PostCustomR2dbcRepositoryImpl implements PostCustomR2dbcRepository{

    private final DatabaseClient databaseClient;

    @Override
    public Flux<Post> findAllByUserId(Long userId) {
        var sql = """
                    select p.id as pid, p.user_id as userId, p.title, p.content, p.created_at as createdAt, p.updated_at as updatedAt,
                        u.id as uid, u.name as name, u.email as email, u.created_at as uCreatedAt, u.updated_at as uUpdatedAt
                    from posts p
                    left join users u on p.user_id = u.id
                    where p.user_id = :userId
                """;
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .fetch()
                .all()
                .flatMap(data -> {
                    return Flux.just(Post.builder()
                                    .id((Long)data.get("pid"))
                                    .userId((Long)data.get("userId"))
                                    .title((String)data.get("title"))
                                    .content((String)data.get("content"))
                                    .user(User.builder()
                                            .id((Long)data.get("uid"))
                                            .name((String)data.get("name"))
                                            .email((String)data.get("email"))
                                            .createdAt(((ZonedDateTime)data.get("uCreatedAt")).toLocalDateTime())
                                            .updatedAt(((ZonedDateTime)data.get("uUpdatedAt")).toLocalDateTime())
                                            .build())
                                    .createdAt(((ZonedDateTime)data.get("createdAt")).toLocalDateTime())
                                    .updatedAt(((ZonedDateTime)data.get("updatedAt")).toLocalDateTime())
                            .build());
                });
    }
}
