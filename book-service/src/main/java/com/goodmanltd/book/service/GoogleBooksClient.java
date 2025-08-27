package com.goodmanltd.book.service;

import com.goodmanltd.book.dto.GoogleBookSearchRes;
import com.goodmanltd.book.dto.GoogleBookVolRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class GoogleBooksClient {

	private RestTemplate restTemplate;

	@Value("${google.apiKey}")
	private String googleApiKey;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public Optional<GoogleBookVolRes> fetchByIsbn(String isbn) {
		String googleSearchUrl = "https://www.googleapis.com/books/v1/volumes?q=isbn:" +isbn +
				"&key=" + googleApiKey;
		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<GoogleBookSearchRes> response = restTemplate.exchange(
					googleSearchUrl,
					HttpMethod.GET,
					null,
					GoogleBookSearchRes.class
			);

			GoogleBookSearchRes searchRes = response.getBody();

			LOGGER.info("google search res: " + searchRes);

			if (searchRes != null && searchRes.getItems() != null && !searchRes.getItems().isEmpty()) {
				// mainly copy textSnippet
				// copy rest in-case get detail fail
				GoogleBookVolRes combinedRes = new GoogleBookVolRes();

				combinedRes.setId(searchRes.getItems().get(0).getId());
				combinedRes.setSelfLink(searchRes.getItems().get(0).getSelfLink());
				combinedRes.setSearchInfo(searchRes.getItems().get(0).getSearchInfo());

				GoogleBookVolRes.VolumeInfo volumeInfo = new GoogleBookVolRes.VolumeInfo();

				// get book detail using self link from search result
				ResponseEntity<GoogleBookVolRes> bkDetailsResponse = restTemplate.exchange(
						searchRes.getItems().get(0).getSelfLink(),
						HttpMethod.GET,
						null,
						GoogleBookVolRes.class
				);
				GoogleBookVolRes detailRes = bkDetailsResponse.getBody();
				LOGGER.info("google detail res:" + detailRes);
				if (detailRes != null) {
					GoogleBookVolRes.VolumeInfo detailInfo = detailRes.getVolumeInfo();
					volumeInfo.setTitle(detailInfo.getTitle());
					volumeInfo.setAuthors(detailInfo.getAuthors());
					volumeInfo.setDescription(detailInfo.getDescription());
					volumeInfo.setLanguage(detailInfo.getLanguage());
					volumeInfo.setImageLinks(detailInfo.getImageLinks());
					volumeInfo.setCategories(detailInfo.getCategories());
				} else {
					// fallback: copy from search result if detail fails
					GoogleBookVolRes.VolumeInfo searchInfo = searchRes.getItems().get(0).getVolumeInfo();
					volumeInfo.setTitle(searchInfo.getTitle());
					volumeInfo.setAuthors(searchInfo.getAuthors());
					volumeInfo.setDescription(searchInfo.getDescription());
					volumeInfo.setLanguage(searchInfo.getLanguage());
					volumeInfo.setImageLinks(searchInfo.getImageLinks());
					volumeInfo.setCategories(searchInfo.getCategories());
				}


				combinedRes.setVolumeInfo(volumeInfo);
				LOGGER.info("final combined res: " + combinedRes);

				return Optional.of(combinedRes);
			}

		} catch (Exception e) {
		    System.out.println(e.getMessage());

		}
		return Optional.empty();
	};
}
