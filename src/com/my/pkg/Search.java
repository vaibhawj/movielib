package com.my.pkg;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

public class Search {

	/**
	 * Extensions case insensitive
	 * 
	 */
	public static Collection<FileInfo> search(List<String> dirs,
			List<String> exts) {

		StringBuilder pattern = new StringBuilder("(?i)");
		for (String ext : exts) {
			pattern.append(".*\\.").append(ext).append("|");
		}

		RegexFileFilter filter = new RegexFileFilter(pattern.substring(0,
				pattern.length() - 1));

		Collection<File> files = new LinkedList<>();

		for (String dir : dirs) {

			File d = new File(dir);
			if (!d.exists() || !d.isDirectory()) {
				continue;
			}

			files.addAll(FileUtils.listFiles(d, filter,
					DirectoryFileFilter.DIRECTORY));

		}

		Collection<FileInfo> searchResult = postSearch(files);

		return searchResult;
	}

	private static Collection<FileInfo> postSearch(Collection<File> files) {

		if (files.size() == 0) {
			return null;
		}

		Collection<FileInfo> fileInfos = new LinkedList<>();
		FileInfo fileInfo = null;

		for (File f : files) {

			fileInfo = new FileInfo();

			String absolutePath = f.getAbsolutePath();
			String ext = FilenameUtils.getExtension(absolutePath);

			fileInfo.setAbsolutePath(absolutePath);
			// fileInfo.setFile(f);
			fileInfo.setFileName(absolutePath);
			fileInfo.setTitle(getTitle(f, ext));
			fileInfo.setGenre(getFileAttribute(absolutePath,
					FileInfo.ATTR_GENRE));
			fileInfo.setRating(getFileAttribute(absolutePath,
					FileInfo.ATTR_RATING));

			fileInfo.setSize(getFileSize(f));
			fileInfo.setCreateDate(getCreateDate(f));
			fileInfo.setFileType(ext.toLowerCase());
			fileInfo.setMovieSeen(getMovieSeenInfo(absolutePath));

			fileInfos.add(fileInfo);
		}

		return fileInfos;

	}

	private static String getTitle(File f, String ext) {
		String title = getFileAttribute(f.getAbsolutePath(),
				FileInfo.ATTR_TITLE);

		if (null == title || FileInfo.NOT_AVAILABLE.equals(title)) {
			title = f.getName().split("." + ext)[0];
		}

		return title;
	}

	/**
	 * @param f
	 * @return
	 */
	private static String getFileSize(File f) {
		double megabytes = f.length() / (1024 * 1024);
		StringBuilder size = new StringBuilder();
		if (megabytes % 1024 >= 1024) {
			double gigabytes = (megabytes / 1024);
			size.append(gigabytes).append(" GB");
		} else {
			size.append(megabytes).append(" MB");
		}
		return size.toString();
	}

	/**
	 * @param f
	 * @return
	 */
	private static String getCreateDate(File f) {
		FileTime createTime = null;
		String createDateTime = FileInfo.NOT_AVAILABLE;

		try {
			BasicFileAttributes attr = Files.readAttributes(f.toPath(),
					BasicFileAttributes.class);
			createTime = attr.creationTime();

			if (null != createTime) {
				createDateTime = createTime.toString().split("T")[0];
			}
		} catch (Exception e) {
			// ignore

		}
		return createDateTime;
	}

	/**
	 * @param f
	 */
	private static String getMovieSeenInfo(String fileAbsPath) {

		String attribute = FileInfo.ATTR_MOVIE_SEEN;

		String value = getFileAttribute(fileAbsPath, attribute);

		if (null == value || FileInfo.NOT_AVAILABLE.equals(value)) {
			value = FileInfo.MOVIE_NOT_SEEN;
		}

		return value;
	}

	/**
	 * @param movieSeen
	 * @param absPath
	 * @return
	 */
	public static boolean setFileAttribute(String fileAbsPath,
			String attribute, String value) {
		File f = new File(fileAbsPath);

		UserDefinedFileAttributeView view = Files.getFileAttributeView(
				f.toPath(), UserDefinedFileAttributeView.class);

		boolean success = true;
		try {
			view.write(attribute, Charset.defaultCharset().encode(value));
		} catch (Exception e1) {
			success = false;
		}
		return success;
	}

	/**
	 * @param movieSeen
	 * @param absPath
	 * @return
	 */
	public static String getFileAttribute(String fileAbsPath, String attribute) {
		String value = FileInfo.NOT_AVAILABLE;
		File f = new File(fileAbsPath);
		Path file = f.toPath();

		UserDefinedFileAttributeView view = Files.getFileAttributeView(file,
				UserDefinedFileAttributeView.class);

		try {
			ByteBuffer buf = null;
			buf = ByteBuffer.allocate(view.size(attribute));
			view.read(attribute, buf);
			buf.flip();
			value = Charset.defaultCharset().decode(buf).toString();

		} catch (Exception e) {
			// ignore
		}

		return value;
	}
}
