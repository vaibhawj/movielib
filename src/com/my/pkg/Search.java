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
			fileInfo.setLocation(f.getParent());
			fileInfo.setFileName(f.getName());
			fileInfo.setSize(getFileSize(f));
			fileInfo.setCreateDate(getCreateDate(f));
			fileInfo.setFileType(ext.toLowerCase());
			fileInfo.setMovieSeen(getMovieSeenInfo(f));

			fileInfos.add(fileInfo);
		}

		return fileInfos;

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
		String createDateTime = "NA";

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
	private static String getMovieSeenInfo(File f) {
		Path file = f.toPath();

		UserDefinedFileAttributeView view = Files.getFileAttributeView(file,
				UserDefinedFileAttributeView.class);

		String name = "user.movieseen";

		// If seen Yes
		String value = FileInfo.MOVIE_NOT_SEEN;

		try {
			ByteBuffer buf = null;
			buf = ByteBuffer.allocate(view.size(name));
			view.read(name, buf);
			buf.flip();
			value = Charset.defaultCharset().decode(buf).toString();

		} catch (Exception e) {
			// ignore
		}

		return value;
	}
}
