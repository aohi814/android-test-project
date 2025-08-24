//
//  BookDetailView.swift
//  iosApp
//
//  Created by Natalie Flores on 5/3/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import shared

struct BookDetailsView: View {
	private var model: DetailModel
	var body: some View {
		VStack {
			AsyncImage(url: URL(string: model.book.imageUrl)) { image in
				image.resizable()
			} placeholder: {
				ProgressView()
			}
			.frame(width: 120, height: 180)
			Text(model.book.title)
				.font(.title)
			Text(model.book.author)
				.font(.title3)
			Text(model.publishDate)
				.font(.footnote)
			Text(model.description_)
				.padding(.vertical)
		}
		.padding()
	}

	init(model: DetailModel) {
		self.model = model
	}
}

#Preview {
	BookDetailsView(
		model: DetailModel(
			book: BookModel(
				title: "The Great Gatsby",
				author: "F. Scott Fitzgerald",
				key: "3",
				imageUrl: ""
			),
			description: "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
			publishDate: "April 10, 1925"
		)
	)
}
