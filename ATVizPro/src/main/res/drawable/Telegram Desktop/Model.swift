//
//  Model.swift
//  Cleaner
//
//  Created by Minh Tường on 15/06/2022.
//

import MTSDK

struct V2Model: Codable {
    let feature, trending: [Feature]
}

// MARK: - Feature
struct Feature: Codable {
    let genreIds, genres: [String]
    let status: String
    let views, rating: Int
    let isCopyrightedComic: Bool
    let urlSource: String
    let author, createdAt, description, name: String
    let source: String
    let thumbnail: String
    let updatedAt, publishDate: String
    let lastChapterDate: String?
    let id: String
    let numberOfChapters: JSONNull?
    let isFollowed: Bool?
}


// MARK: - Encode/decode helpers
class JSONNull: Codable, Hashable {

    public static func == (lhs: JSONNull, rhs: JSONNull) -> Bool {
        return true
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(0)
    }

    public init() {}

    public required init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if !container.decodeNil() {
            throw DecodingError.typeMismatch(JSONNull.self, DecodingError.Context(codingPath: decoder.codingPath, debugDescription: "Wrong type for JSONNull"))
        }
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        try container.encodeNil()
    }
}
