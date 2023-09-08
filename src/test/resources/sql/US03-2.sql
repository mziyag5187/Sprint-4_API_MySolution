SELECT *
FROM books;

# US-03-2
SELECT b.name as 'bookName' , b.isbn, b.year, b.author, bc.name as 'bookCategoryName'
FROM books b
         inner join book_categories bc
                    on b.book_category_id = bc.id
where b.author = 'Dr. Kerrie Stehr';




