package com.example.bookshelfdemo.book;

class BookNotFoundException extends RuntimeException {

    BookNotFoundException(Long id) {
        super(String.format("Book with id: %d not found", id));
    }
}
