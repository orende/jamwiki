/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.parser.image;

import java.io.File;
import org.jamwiki.Environment;
import org.jamwiki.JAMWikiUnitTest;
import org.jamwiki.WikiBase;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageUtilTest extends JAMWikiUnitTest {

	@Test
	public void testBuildImageLinkHtml() throws Throwable {
		ImageMetadata imageMetadata = new ImageMetadata();
		imageMetadata.setLink("");
		String actualResult = ImageUtil.buildImageLinkHtml("/wiki", "en", "File:Test Image.jpg", imageMetadata, null, true, null);
		String expectedResult = "<img class=\"wikiimg\" src=\"/files/test_image.jpg\" width=\"400\" height=\"267\" alt=\"File:Test Image.jpg\" />";
		assertThat(expectedResult).isEqualTo(actualResult); // "Image link HTML built incorrectly"
	}

	@Test
	public void testBuildImageFileDocrootUrl() {
		String imgUrlRelative = ImageUtil.buildImageFileUrl("/wiki", "en", "File:Test Image.jpg", false);
		assertThat(imgUrlRelative).isEqualTo("/files/test_image.jpg"); // "Relative image link URL incorrect"
		String imgUrlAbsolute = ImageUtil.buildImageFileUrl("/wiki", "en", "File:Test Image.jpg", true);
		assertThat(imgUrlAbsolute).isEqualTo("http://example.com/files/test_image.jpg"); // "Absolute image link URL incorrect"
		String originalFileServerUrl = Environment.getValue(Environment.PROP_FILE_SERVER_URL);
		try {
			Environment.setValue(Environment.PROP_FILE_SERVER_URL, "http://media.example.com");
			String imUrlFileServerRelative = ImageUtil.buildImageFileUrl("/wiki", "en", "File:Test Image.jpg", false);
            assertThat(imUrlFileServerRelative).isEqualTo("http://media.example.com/files/test_image.jpg"); // "Alternate image link URL incorrect"
			String imgUrlFileServerAbsolute = ImageUtil.buildImageFileUrl("/wiki", "en", "File:Test Image.jpg", true);
			assertThat(imgUrlFileServerAbsolute).isEqualTo("http://media.example.com/files/test_image.jpg"); // "Alternate image link URL (forced) incorrect"
			Environment.setValue(Environment.PROP_FILE_SERVER_URL, "//media.example.com");
			String imgUrlFileServerRelativeNoProtocol = ImageUtil.buildImageFileUrl("/wiki", "en", "File:Test Image.jpg", false);
            assertThat(imgUrlFileServerRelativeNoProtocol).isEqualTo("//media.example.com/files/test_image.jpg"); // "Alternate image link URL (no protocol) incorrect"
		} finally {
			Environment.setValue(Environment.PROP_FILE_SERVER_URL, originalFileServerUrl);
		}
	}

	@Test
	public void testBuildImageDefaultUrl() {
		String originalFileUploadStorage = Environment.getValue(Environment.PROP_FILE_UPLOAD_STORAGE);
		String FILE_NAME = "/2010/10/example.jpg";
		try {
			Environment.setValue(Environment.PROP_FILE_UPLOAD_STORAGE, WikiBase.UPLOAD_STORAGE.JAMWIKI.toString());
			String actualResult = ImageUtil.buildImageUrl("/wiki", FILE_NAME, false);
			String expectedResult = "/wiki/uploads" + FILE_NAME;
			assertThat(expectedResult).withFailMessage("Relative image link URL incorrect").isEqualTo(actualResult);
		} finally {
			Environment.setValue(Environment.PROP_FILE_UPLOAD_STORAGE, originalFileUploadStorage);
		}
	}

	@Test
	public void testBuildImageDatabaseUrl() {
		String originalFileUploadStorage = Environment.getValue(Environment.PROP_FILE_UPLOAD_STORAGE);
		String FILE_NAME = "/this-should-be-updated";
		try {
			Environment.setValue(Environment.PROP_FILE_UPLOAD_STORAGE, WikiBase.UPLOAD_STORAGE.DATABASE.toString());
			String actualResult = ImageUtil.buildImageUrl("/wiki", FILE_NAME, false);
			String expectedResult = "/wiki/uploads" + FILE_NAME;
			assertThat(expectedResult).withFailMessage("Relative image link URL incorrect").isEqualTo(actualResult);
			File resultFile = ImageUtil.buildAbsoluteFile(FILE_NAME);
			assertThat(resultFile).withFailMessage("Absolute image link URL incorrect").isNull();
		} finally {
			Environment.setValue(Environment.PROP_FILE_UPLOAD_STORAGE, originalFileUploadStorage);
		}
	}
}
