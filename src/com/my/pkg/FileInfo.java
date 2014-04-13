package com.my.pkg;

public class FileInfo {

	final protected static String MOVIE_SEEN = "Yes";
	final protected static String MOVIE_NOT_SEEN = "No";

	private String absolutePath;

	// private File file;

	private String location;

	private String fileName;

	private String size;

	private String createDate;

	private String fileType;

	private String movieSeen;

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
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

}
