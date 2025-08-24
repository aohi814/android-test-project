import SwiftUI
import shared

public struct ContentView: View {
	@State private var searchText = ""
	@State private var searchViewModel = SearchViewModel()
	@State private var works: Works?
	@State private var showToast = false

	public var body: some View {
		NavigationStack {
			BookListView(
				books: works?.books ?? []
			)
		}
		.searchable(text: $searchText)
		.onSubmit(of: .search) {
			Task.init {
				await fetchBooks()
			}
		}
		.toast(timestamp: works?.timeStamp, isPresented: $showToast)
		.onAppear {
			showToast = works?.timeStamp != nil
		}
	}

	func fetchBooks() async {
		do {
			works = try await searchViewModel.searchBySubject(
				token: searchText
			)
		} catch _ { }
	}
}

struct ToastModifier: ViewModifier {
	let timestamp: String?
	@Binding var isPresented: Bool

	private let animationDelay = 1.0

	func body(content: Content) -> some View {
		ZStack {
			content
				.transaction { $0.animation = .none }

			if let timestamp = self.timestamp, isPresented {
				ZStack {
					Text("Last Updated: \(timestamp)")
						.font(.system(size: 12))
						.padding()
						.foregroundColor(.white)
						.background(.blue)
						.clipShape(RoundedRectangle(cornerRadius: 30))
						.transition(.move(edge: .bottom).combined(with: .opacity))
						.onTapGesture {
							withAnimation {
								isPresented = false
							}
						}
						.onAppear {
							Task {
								try? await Task.sleep(for: .seconds(2))
								await MainActor.run {
									withAnimation(.easeInOut(duration: animationDelay)) {
										isPresented = false
									}
								}
							}
						}
						.animation(.easeInOut(duration: animationDelay), value: isPresented)
				}
				.frame(maxHeight: .infinity, alignment: .bottom)
				.shadow(color: .gray, radius: 4, x: 0, y: 2)
				.padding(.vertical, 15)
			}
		}
	}
}

extension View {
	func toast(timestamp: String?, isPresented: Binding<Bool>) -> some View {
		self.modifier(ToastModifier(timestamp: timestamp, isPresented: isPresented))
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
