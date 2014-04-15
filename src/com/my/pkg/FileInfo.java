package com.my.pkg;

public class FileInfo {

	final protected static String MOVIE_SEEN = "Yes";
	final protected static String MOVIE_NOT_SEEN = "No";
	final protected static String ATTR_MOVIE_SEEN = "user.movieseen";
	final protected static String ATTR_TITLE = "user.title";
	final protected static String ATTR_GENRE = "user.genre";
	final protected static String ATTR_RATING = "user.rating";
	final protected static String NOT_AVAILABLE = "N/A";

	private String absolutePath;

	// private File file;

	private String title;

	private String fileName;

	private String size;

	private String createDate;

	private String fileType;

	private String movieSeen;

	private String genre;

	private String rating;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	/*
	 * public File getFile() { return file; }
	 * 
	 * public void setFile(File file) { this.file = file; }
	 */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((absolutePath == null) ? 0 : absolutePath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof FileInfo)) {
			return false;
		}
		FileInfo other = (FileInfo) obj;
		if (absolutePath == null) {
			if (other.absolutePath != null) {
				return false;
			}
		} else if (!absolutePath.equals(other.absolutePath)) {
			return false;
		}
		return true;
	}

	public String getMovieSeen() {
		return movieSeen;
	}

	public void setMovieSeen(String movieSeen) {
		this.movieSeen = movieSeen;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

}
