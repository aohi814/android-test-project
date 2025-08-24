//
//  BookListView.swift
//  iosApp
//
//  Created by Natalie Flores on 5/3/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import shared

struct BookListView: View {
	private var books: [BookModel]
	@State private var getDetailViewModel = DetailViewModel()
	@State private var selectedBook: DetailModel?

	var body: some View {
		List {
			ForEach(books, id: \.key) { book in
				BookItemView(book: book)
					.onTapGesture {
						Task.init {
							await fetchDetails(book: book)
						}
					}
			}
		}
		.navigationDestination(item: $selectedBook) { book in
			BookDetailsView(model: book)
		}
	}

	init(books: [BookModel]) {
		self.books = books
	}

	func fetchDetails(book: BookModel) async {
		do {
			selectedBook = try await getDetailViewModel.getDetails(
				key: book.key
			)
		} catch _ { }
	}
}

struct BookItemView: View {
	private var book: BookModel

	var body: some View {
		HStack {
			AsyncImage(url: URL(string: book.imageUrl)) { image in
				image.resizable()
			} placeholder: {
				ProgressView()
			}
			.frame(width: 40, height: 60)
			VStack(alignment: .leading) {
				Text(book.title)
				Text(book.author)
					.font(.caption)
			}
		}
	}

	init(book: BookModel) {
		self.book = book
	}
}

#Preview {
	NavigationStack {
		BookListView(
			books: [
				BookModel(
					title: "The Odyssey",
					author: "Homer",
					key: "1",
					imageUrl: ""
				),
				BookModel(
					title: "Pride and Prejudice",
					author: "Jane Austen",
					key: "2",
					imageUrl: ""
				),
				BookModel(
					title: "The Great Gatsby",
					author: "F. Scott Fitzgerald",
					key: "3",
					imageUrl: ""
				),
			]
		)
	}
}
