package com.example.bookshelfdemo.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class BookController {

    private final BookRepository bookRepository;

    @Autowired
    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/books")
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @GetMapping("/books/{id}")
    public Book findById(@PathVariable Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    @PostMapping("/books")
    public Book save(@RequestBody @Valid Book book) {
        book.setIsbn(ISBNParser.parse(book.getIsbn()));
        return bookRepository.save(book);
    }

    @PutMapping("/books/{id}")
    public Book update(@RequestBody @Valid Book book, @PathVariable Long id) {
        var bookForUpdate = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        bookForUpdate.setIsbn(ISBNParser.parse(book.getIsbn()));
        bookForUpdate.setNumberOfPages(book.getNumberOfPages());
        bookForUpdate.setRating(book.getRating());
        bookForUpdate.setAuthor(book.getAuthor());
        bookForUpdate.setTitle(book.getTitle());
        return bookRepository.save(bookForUpdate);
    }

    @DeleteMapping("/books/{id}")
    public void delete(@PathVariable Long id) {
        bookRepository.findById(id).ifPresentOrElse(bookRepository::delete, () -> {
            throw new BookNotFoundException(id);
        });
    }

}
