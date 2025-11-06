package com.wafflestudio.spring2025

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.spring2025.course.model.Course
import com.wafflestudio.spring2025.course.model.CourseTimeSlot
import com.wafflestudio.spring2025.course.repository.CourseRepository
import com.wafflestudio.spring2025.course.repository.CourseTimeSlotRepository
import com.wafflestudio.spring2025.helper.DataGenerator
import com.wafflestudio.spring2025.timetable.dto.AddCourseRequest
import com.wafflestudio.spring2025.timetable.dto.CreateTimeTableRequest
import com.wafflestudio.spring2025.timetable.dto.UpdateTimeTableRequest
import com.wafflestudio.spring2025.timetable.model.Semester
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.DayOfWeek

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TimetableIntegrationTest
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val dataGenerator: DataGenerator,
        private val courseRepository: CourseRepository,
        private val courseTimeSlotRepository: CourseTimeSlotRepository,
    ) {
        private lateinit var course1: Course
        private lateinit var course2: Course
        private lateinit var course3: Course

        @BeforeEach
        fun setup() {
            // Seed courses for testing (Monday 9:00-10:15)
            course1 =
                courseRepository.save(
                    Course(
                        year = 2025,
                        term = "FALL",
                        category = "전공",
                        college = "공과대학",
                        department = "컴퓨터공학부",
                        program = "학사",
                        grade = 3,
                        rawTime = "월(09:00~10:15)",
                        courseCode = "M1522.000100",
                        classCode = "001",
                        title = "데이터구조",
                        credit = 3,
                        professor = "홍길동",
                        room = "302동 301호",
                    ),
                )
            courseTimeSlotRepository.save(
                CourseTimeSlot(
                    courseId = course1.id!!,
                    day = DayOfWeek.MONDAY,
                    startMin = 540, // 9:00
                    endMin = 615, // 10:15
                ),
            )

            // Tuesday 13:00-14:15
            course2 =
                courseRepository.save(
                    Course(
                        year = 2025,
                        term = "FALL",
                        category = "전공",
                        college = "공과대학",
                        department = "컴퓨터공학부",
                        program = "학사",
                        grade = 3,
                        rawTime = "화(13:00~14:15)",
                        courseCode = "M1522.000200",
                        classCode = "001",
                        title = "알고리즘",
                        credit = 3,
                        professor = "김철수",
                        room = "302동 309호",
                    ),
                )
            courseTimeSlotRepository.save(
                CourseTimeSlot(
                    courseId = course2.id!!,
                    day = DayOfWeek.TUESDAY,
                    startMin = 780, // 13:00
                    endMin = 855, // 14:15
                ),
            )

            // Monday 9:30-11:00 (overlaps with course1)
            course3 =
                courseRepository.save(
                    Course(
                        year = 2025,
                        term = "FALL",
                        category = "교양",
                        college = "인문대학",
                        department = "철학과",
                        program = "학사",
                        grade = 1,
                        rawTime = "월(09:30~11:00)",
                        courseCode = "L0440.000100",
                        classCode = "001",
                        title = "논리와 비판적 사고",
                        credit = 3,
                        professor = "이영희",
                        room = "220동 201호",
                    ),
                )
            courseTimeSlotRepository.save(
                CourseTimeSlot(
                    courseId = course3.id!!,
                    day = DayOfWeek.MONDAY,
                    startMin = 570, // 9:30
                    endMin = 660, // 11:00
                ),
            )
        }

        @Test
        fun `should create a timetable`() {
            // 시간표를 생성할 수 있다
            val (_, token) = dataGenerator.generateUser()
            val request = CreateTimeTableRequest(name = "2025 가을학기", year = 2025, semester = Semester.FALL)

            mvc
                .perform(
                    post("/api/v1/timetable")
                        .header("Authorization", "Bearer $token")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("2025 가을학기"))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.semester").value("FALL"))
        }

        @Test
        fun `should retrieve all own timetables`() {
            // 자신의 모든 시간표 목록을 조회할 수 있다
            val (user, token) = dataGenerator.generateUser()
            dataGenerator.generateTimeTable(name = "시간표1", user = user)
            dataGenerator.generateTimeTable(name = "시간표2", user = user)

            mvc
                .perform(
                    get("/api/v1/timetable")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists())
        }

        @Test
        fun `should retrieve timetable details`() {
            // 시간표 상세 정보를 조회할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimeTable(name = "내 시간표", user = user)

            mvc
                .perform(
                    get("/api/v1/timetable/${timetable.id}")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.timetable.name").value("내 시간표"))
                .andExpect(jsonPath("$.timetable.year").value(2025))
                .andExpect(jsonPath("$.timetable.semester").value("FALL"))
                .andExpect(jsonPath("$.totalCredits").value(0))
                .andExpect(jsonPath("$.courses").isArray)
        }

        @Test
        fun `should update timetable name`() {
            // 시간표 이름을 수정할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimeTable(name = "원래 이름", user = user)
            val request = UpdateTimeTableRequest(name = "새로운 이름")

            mvc
                .perform(
                    patch("/api/v1/timetable/${timetable.id}")
                        .header("Authorization", "Bearer $token")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("새로운 이름"))
        }

        @Test
        fun `should not update another user's timetable`() {
            // 다른 사람의 시간표는 수정할 수 없다
            val (user1, _) = dataGenerator.generateUser()
            val (_, token2) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimeTable(name = "user1의 시간표", user = user1)
            val request = UpdateTimeTableRequest(name = "해킹 시도")

            mvc
                .perform(
                    patch("/api/v1/timetable/${timetable.id}")
                        .header("Authorization", "Bearer $token2")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isForbidden)
        }

        @Test
        fun `should delete a timetable`() {
            // 시간표를 삭제할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimeTable(name = "삭제될 시간표", user = user)

            mvc
                .perform(
                    delete("/api/v1/timetable/${timetable.id}")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isNoContent)
        }

        @Test
        fun `should not delete another user's timetable`() {
            // 다른 사람의 시간표는 삭제할 수 없다
            val (user1, _) = dataGenerator.generateUser()
            val (_, token2) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimeTable(name = "user1의 시간표", user = user1)

            mvc
                .perform(
                    delete("/api/v1/timetable/${timetable.id}")
                        .header("Authorization", "Bearer $token2")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isForbidden)
        }

        @Test
        fun `should search for courses`() {
            // 강의를 검색할 수 있다
            mvc
                .perform(
                    get("/api/v1/courses")
                        .param("year", "2025")
                        .param("semester", "FALL")
                        .param("query", "데이터")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isArray)
                .andExpect(jsonPath("$.content[0].title").value("데이터구조"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
        }

        @Test
        fun `should add a course to timetable`() {
            // 시간표에 강의를 추가할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimeTable(name = "내 시간표", year = 2025, semester = Semester.FALL, user = user)
            val request = AddCourseRequest(courseId = course1.id!!)

            mvc
                .perform(
                    post("/api/v1/timetable/${timetable.id}/courses")
                        .header("Authorization", "Bearer $token")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.courses.length()").value(1))
                .andExpect(jsonPath("$.courses[0].title").value("데이터구조"))
                .andExpect(jsonPath("$.totalCredits").value(3))
        }

        @Test
        fun `should return error when adding overlapping course to timetable`() {
            // 시간표에 강의 추가 시, 시간이 겹치면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimeTable(name = "내 시간표", year = 2025, semester = Semester.FALL, user = user)

            // Add course1 first (Monday 9:00-10:15)
            val request1 = AddCourseRequest(courseId = course1.id!!)
            mvc
                .perform(
                    post("/api/v1/timetable/${timetable.id}/courses")
                        .header("Authorization", "Bearer $token")
                        .content(mapper.writeValueAsString(request1))
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)

            // Try to add course3 (Monday 9:30-11:00) - should overlap
            val request2 = AddCourseRequest(courseId = course3.id!!)
            mvc
                .perform(
                    post("/api/v1/timetable/${timetable.id}/courses")
                        .header("Authorization", "Bearer $token")
                        .content(mapper.writeValueAsString(request2))
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isConflict)
        }

        @Test
        fun `should not add a course to another user's timetable`() {
            // 다른 사람의 시간표에는 강의를 추가할 수 없다
            val (user1, _) = dataGenerator.generateUser()
            val (_, token2) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimeTable(name = "user1의 시간표", user = user1)
            val request = AddCourseRequest(courseId = course1.id!!)

            mvc
                .perform(
                    post("/api/v1/timetable/${timetable.id}/courses")
                        .header("Authorization", "Bearer $token2")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isForbidden)
        }

        @Test
        fun `should remove a course from timetable`() {
            // 시간표에서 강의를 삭제할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimeTable(name = "내 시간표", year = 2025, semester = Semester.FALL, user = user)

            // Add course first
            val addRequest = AddCourseRequest(courseId = course1.id!!)
            mvc
                .perform(
                    post("/api/v1/timetable/${timetable.id}/courses")
                        .header("Authorization", "Bearer $token")
                        .content(mapper.writeValueAsString(addRequest))
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)

            // Remove course
            mvc
                .perform(
                    delete("/api/v1/timetable/${timetable.id}/courses/${course1.id}")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.courses.length()").value(0))
                .andExpect(jsonPath("$.totalCredits").value(0))
        }

        @Test
        fun `should not remove a course from another user's timetable`() {
            // 다른 사람의 시간표에서는 강의를 삭제할 수 없다
            val (user1, token1) = dataGenerator.generateUser()
            val (_, token2) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimeTable(name = "user1의 시간표", user = user1)

            // user1 adds course
            val addRequest = AddCourseRequest(courseId = course1.id!!)
            mvc
                .perform(
                    post("/api/v1/timetable/${timetable.id}/courses")
                        .header("Authorization", "Bearer $token1")
                        .content(mapper.writeValueAsString(addRequest))
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)

            // user2 tries to remove course
            mvc
                .perform(
                    delete("/api/v1/timetable/${timetable.id}/courses/${course1.id}")
                        .header("Authorization", "Bearer $token2")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isForbidden)
        }

        @Test
        @Disabled("곧 안내드리겠습니다")
        fun `should fetch and save course information from SNU course registration site`() {
            // 서울대 수강신청 사이트에서 강의 정보를 가져와 저장할 수 있다
        }

        @Test
        fun `should return correct course list and total credits when retrieving timetable details`() {
            // 시간표 상세 조회 시, 강의 정보 목록과 총 학점이 올바르게 반환된다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimeTable(name = "내 시간표", year = 2025, semester = Semester.FALL, user = user)

            // Add course1 (3 credits) and course2 (3 credits)
            mvc
                .perform(
                    post("/api/v1/timetable/${timetable.id}/courses")
                        .header("Authorization", "Bearer $token")
                        .content(mapper.writeValueAsString(AddCourseRequest(courseId = course1.id!!)))
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)

            mvc
                .perform(
                    post("/api/v1/timetable/${timetable.id}/courses")
                        .header("Authorization", "Bearer $token")
                        .content(mapper.writeValueAsString(AddCourseRequest(courseId = course2.id!!)))
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)

            // Retrieve timetable details
            mvc
                .perform(
                    get("/api/v1/timetable/${timetable.id}")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.courses.length()").value(2))
                .andExpect(jsonPath("$.totalCredits").value(6)) // 3 + 3 = 6
                .andExpect(jsonPath("$.courses[0].title").exists())
                .andExpect(jsonPath("$.courses[1].title").exists())
        }

        @Test
        fun `should paginate correctly when searching for courses`() {
            // 강의 검색 시, 페이지네이션이 올바르게 동작한다
            // Create more courses for pagination testing
            for (i in 1..15) {
                courseRepository.save(
                    Course(
                        year = 2025,
                        term = "FALL",
                        category = "전공",
                        college = "공과대학",
                        department = "컴퓨터공학부",
                        program = "학사",
                        grade = 3,
                        rawTime = null,
                        courseCode = "TEST.00${i.toString().padStart(4, '0')}",
                        classCode = "001",
                        title = "Test Course $i",
                        credit = 3,
                        professor = "Professor $i",
                        room = "Room $i",
                    ),
                )
            }

            // Test page 0, size 10
            mvc
                .perform(
                    get("/api/v1/courses")
                        .param("year", "2025")
                        .param("semester", "FALL")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(18)) // 3 seed courses + 15 new = 18
                .andExpect(jsonPath("$.totalPages").value(2)) // ceil(18/10) = 2

            // Test page 1, size 10
            mvc
                .perform(
                    get("/api/v1/courses")
                        .param("year", "2025")
                        .param("semester", "FALL")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(8)) // Remaining 8 courses
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
        }
    }
