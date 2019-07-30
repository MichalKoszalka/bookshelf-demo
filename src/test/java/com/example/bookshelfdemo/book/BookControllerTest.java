package com.example.bookshelfdemo.book;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private BookRepository bookRepository;

    private static Stream<Arguments> saveBookRequestStatusAndResponseParams() {
        return Stream.of(
                Arguments.of("save/correct_book.json", HttpStatus.OK, null, 1),
                Arguments.of("save/correct_book_with_x.json", HttpStatus.OK, null, 1),
                Arguments.of("save/book_incorrect_isbn.json", HttpStatus.BAD_REQUEST, "error/incorrect_isbn.json", 0),
                Arguments.of("save/book_rating_to_high.json", HttpStatus.BAD_REQUEST, "error/incorrect_rating.json", 0),
                Arguments.of("save/book_rating_to_low.json", HttpStatus.BAD_REQUEST, "error/incorrect_rating.json", 0),
                Arguments.of("save/book_no_rating.json", HttpStatus.BAD_REQUEST, "error/no_rating.json", 0),
                Arguments.of("save/book_no_pages.json", HttpStatus.BAD_REQUEST, "error/no_pages.json", 0),
                Arguments.of("save/book_pages_to_low.json", HttpStatus.BAD_REQUEST, "error/pages_to_low.json", 0),
                Arguments.of("save/book_no_author.json", HttpStatus.BAD_REQUEST, "error/no_author.json", 0),
                Arguments.of("save/book_no_title.json", HttpStatus.BAD_REQUEST, "error/no_title.json", 0)
        );
    }

    private static Stream<Arguments> findAllBooksStatusAndResponseParams() {
        return Stream.of(
                Arguments.of(List.of(new Book(1L, "9781617290459", "A", "X", 12, 3), new Book(2L, "9781617290473", "B", "RRR", 12, 3)), HttpStatus.OK, "findAll/books.json"),
                Arguments.of(Collections.emptyList(), HttpStatus.OK, "findAll/no_books.json")
        );
    }

    @ParameterizedTest
    @MethodSource("saveBookRequestStatusAndResponseParams")
    void saveBook_WithGivenRequest_ExpectStatusAndResponse(String requestPath, HttpStatus status, String responsePath, int numberOfSaveCalls) throws Exception {
        // given
        var requestBody = readJson(requestPath);
        var responseBody = responsePath != null ? readJson(responsePath) : null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        //when
        var response = restTemplate.postForEntity("/books", request, String.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(status);
        JSONAssert.assertEquals(responseBody, response.getBody(), JSONCompareMode.LENIENT);
        verify(bookRepository, times(numberOfSaveCalls)).save(any(Book.class));
    }

    @ParameterizedTest
    @MethodSource("findAllBooksStatusAndResponseParams")
    void findBooks_WithBooksSavedBefore_ExpectStatusAndResponse(List<Book> books, HttpStatus status, String responsePath) throws Exception {
        // given
        when(bookRepository.findAll()).thenReturn(books);
        var responseBody = responsePath != null ? readJson(responsePath) : null;

        //when
        var response = restTemplate.getForEntity("/books", String.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(status);
        JSONAssert.assertEquals(responseBody, response.getBody(), JSONCompareMode.LENIENT);
    }

    @Test
    void findBookById_EntityNotExist_BookNotFoundThrownAnd404Returned() throws Exception {
        // given
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        //when
        var response = restTemplate.getForEntity("/books/1", String.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JSONAssert.assertEquals(readJson("error/book_not_found.json"), response.getBody(), JSONCompareMode.LENIENT);
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    void findBookById_EntityExist_BookReturned() throws Exception {
        // given
        var book = new Book(1L, "9781617290459", "A", "X", 12, 3);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        //when
        var response = restTemplate.getForEntity("/books/1", String.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(readJson("findById/book.json"), response.getBody(), JSONCompareMode.LENIENT);
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    void deleteBook_EntityNotExist_BookNotFoundThrownAnd404Returned() throws Exception {
        // given
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        var request = new HttpEntity<>(null);

        //when
        var response = restTemplate.exchange("/books/1", HttpMethod.DELETE, request, String.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JSONAssert.assertEquals(readJson("error/book_not_found.json"), response.getBody(), JSONCompareMode.LENIENT);
        verify(bookRepository, times(0)).deleteById(any());
    }

    @Test
    void deleteBook_EntityExist_BookDeleted() {
        // given
        var book = new Book(1L, "9781617290459", "A", "X", 12, 3);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        var request = new HttpEntity<>(null);

        //when
        var response = restTemplate.exchange("/books/1", HttpMethod.DELETE, request, String.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void updateBook_EntityNotExist_BookNotFoundThrownAnd404Returned() throws Exception {
        // given
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        var headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        var request = new HttpEntity<>(readJson("update/book.json"), headers);

        //when
        var response = restTemplate.exchange("/books/1", HttpMethod.PUT, request, String.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JSONAssert.assertEquals(readJson("error/book_not_found.json"), response.getBody(), JSONCompareMode.LENIENT);
        verify(bookRepository, times(0)).save(any());
    }

    @Test
    void updateBook_EntityExist_BookUpdated() throws Exception {
        // given
        var book = new Book(1L, "9781617290459", "A", "X", 12, 3);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        var bookForUpdate = new Book(1L, "9783161484100", "Lord of the Rings", "J.R.R. Tolkien", 11 ,5);
        when(bookRepository.save(bookForUpdate)).thenReturn(bookForUpdate);
        var headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        var request = new HttpEntity<>(readJson("update/book.json"), headers);

        //when
        var response = restTemplate.exchange("/books/1", HttpMethod.PUT, request, String.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bookRepository, times(1)).save(any(Book.class));
        JSONAssert.assertEquals(readJson("update/book.json"), response.getBody(), JSONCompareMode.LENIENT);
    }

    private String readJson(String fileName) throws Exception {
        return FileUtils.readFileToString(new File(getClass().getClassLoader().getResource(fileName).getFile()), Charset.defaultCharset());
    }
}
