package com.wafflestudio.spring2025.post.controller

import com.wafflestudio.spring2025.post.dto.CreatePostRequest
import com.wafflestudio.spring2025.post.dto.CreatePostResponse
import com.wafflestudio.spring2025.post.dto.PostPagingResponse
import com.wafflestudio.spring2025.post.dto.UpdatePostRequest
import com.wafflestudio.spring2025.post.dto.UpdatePostResponse
import com.wafflestudio.spring2025.post.dto.core.PostDto
import com.wafflestudio.spring2025.post.service.PostService
import com.wafflestudio.spring2025.user.LoggedInUser
import com.wafflestudio.spring2025.user.model.User
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class PostController(
    private val postService: PostService,
) {
    @PostMapping("/api/v1/boards/{boardId}/posts")
    fun create(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @PathVariable boardId: Long,
        @RequestBody createRequest: CreatePostRequest,
    ): ResponseEntity<CreatePostResponse> {
        val postDto =
            postService.create(
                title = createRequest.title,
                content = createRequest.content,
                user = user,
                boardId = boardId,
            )
        return ResponseEntity.ok(postDto)
    }

    @GetMapping("/api/v1/boards/{boardId}/posts")
    fun page(
        @PathVariable boardId: Long,
        @RequestParam(value = "nextCreatedAt", required = false) nextCreatedAt: Long?,
        @RequestParam(value = "nextId", required = false) nextId: Long?,
        @RequestParam(value = "limit", defaultValue = "10") limit: Int,
    ): ResponseEntity<PostPagingResponse> {
        val postPagingResponse =
            postService.pageByBoardId(
                boardId,
                nextCreatedAt?.let { Instant.ofEpochMilli(it) },
                nextId,
                limit,
            )
        return ResponseEntity.ok(postPagingResponse)
    }

    @GetMapping("/api/v1/posts/{id}")
    fun get(
        @PathVariable id: Long,
    ): ResponseEntity<PostDto> {
        val postDto = postService.get(id)
        return ResponseEntity.ok(postDto)
    }

    @PatchMapping("/api/v1/posts/{id}")
    fun update(
        @PathVariable id: Long,
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestBody updateRequest: UpdatePostRequest,
    ): ResponseEntity<UpdatePostResponse> {
        val postDto =
            postService.update(
                postId = id,
                title = updateRequest.title,
                content = updateRequest.content,
                user = user,
            )
        return ResponseEntity.ok(postDto)
    }

    @DeleteMapping("/api/v1/posts/{id}")
    fun delete(
        @PathVariable id: Long,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<Unit> {
        postService.delete(id, user)
        return ResponseEntity.noContent().build()
    }
}
