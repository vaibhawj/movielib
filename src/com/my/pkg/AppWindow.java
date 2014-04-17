package com.my.pkg;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AppWindow {

	private JFrame frmMovLib;
	private JList driveList;
	private JList fileTypelist;
	private JLabel lblRoots;
	private JButton btnSearch;
	private JTable table;
	private DefaultTableModel model;

	private static final Object[] columnNames = new Object[] { "File Name",
			"Title", "Size", "Create Date", "File Type", "Movie Seen", "Genre",
			"Rating" };
	private JLabel noOfRec;
	private JLabel lblFileTypes;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					AppWindow window = new AppWindow();
					window.frmMovLib.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AppWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		try {
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			// ignore
		}
		frmMovLib = new JFrame();

		try {
			InputStream in = getClass().getResourceAsStream("img/movlib.png");
			ImageIcon img = new ImageIcon(ImageIO.read(in));
			frmMovLib.setIconImage(img.getImage());
		} catch (Exception e) {
			// ignore
		}

		frmMovLib.setResizable(false);
		frmMovLib.setTitle("Movies Library");
		frmMovLib.setBounds(100, 100, 785, 528);
		frmMovLib.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmMovLib.getContentPane().setLayout(null);

		lblRoots = new JLabel("Roots:");
		lblRoots.setBounds(10, 27, 46, 14);
		frmMovLib.getContentPane().add(lblRoots);

		initSearchButton();

		JSeparator separator = new JSeparator();
		separator.setBounds(0, 110, 779, 1);
		frmMovLib.getContentPane().add(separator);

		initDriveList();

		initTable();

		JLabel lblRec = new JLabel("REC:");
		lblRec.setBounds(671, 85, 30, 14);
		frmMovLib.getContentPane().add(lblRec);

		noOfRec = new JLabel("0");
		noOfRec.setForeground(Color.BLUE);
		noOfRec.setBounds(699, 85, 70, 14);
		frmMovLib.getContentPane().add(noOfRec);

		lblFileTypes = new JLabel("File Types:");
		lblFileTypes.setBounds(179, 27, 59, 14);
		frmMovLib.getContentPane().add(lblFileTypes);
		initFileTypeList();

		initImdbButton();

	}

	/**
	 * 
	 */
	private void initImdbButton() {
		JButton imdbButton = new JButton();
		imdbButton
				.setToolTipText("Click here to fetch rating and other info from IMDb. Make sure Title is correct");
		try {
			InputStream in = getClass().getResourceAsStream("img/imdb.png");
			ImageIcon img = new ImageIcon(ImageIO.read(in));
			imdbButton.setIcon(img);
			imdbButton.setOpaque(false);
			imdbButton.setContentAreaFilled(false);
			imdbButton.setBorderPainted(false);
			in = getClass().getResourceAsStream("img/imdb_pressed.png");
			img = new ImageIcon(ImageIO.read(in));
			imdbButton.setPressedIcon(img);

		} catch (Exception e) {
			imdbButton.setText("IMDb");

		}
		imdbButton.setBounds(723, 11, 46, 23);

		imdbButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				frmMovLib.setCursor(Cursor
						.getPredefinedCursor(Cursor.WAIT_CURSOR));

				int rowCount = table.getModel().getRowCount();
				int row;
				String title = null;
				String value = null;
				String imdbDone = null;
				for (int i = 0; i < rowCount; i++) {
					try {
						row = table.convertRowIndexToModel(i);
						String absPath = (String) table.getModel().getValueAt(
								row, 0);
						imdbDone = Search.getFileAttribute(absPath,
								FileInfo.ATTR_IMDB_DONE);

						if (null != imdbDone
								&& !FileInfo.NOT_AVAILABLE.equals(imdbDone)) {
							// imdb data is alrdy there for
							continue;
						}

						title = (String) table.getModel().getValueAt(row, 1);

						URL url = new URL("http://www.omdbapi.com/?t="
								+ title.trim().replaceAll(" ", "+"));

						URLConnection con = url.openConnection();
						InputStream in = con.getInputStream();
						String encoding = con.getContentEncoding();
						encoding = encoding == null ? "UTF-8" : encoding;
						String body = IOUtils.toString(in, encoding);
						JSONObject json = (JSONObject) new JSONParser()
								.parse(body);

						value = (String) json.get("Response");
						if ("False".equalsIgnoreCase(value)) {
							Search.setFileAttribute(absPath,
									FileInfo.ATTR_GENRE, FileInfo.NOT_AVAILABLE);
							table.getModel().setValueAt(FileInfo.NOT_AVAILABLE,
									row, 6);

							Search.setFileAttribute(absPath,
									FileInfo.ATTR_RATING,
									FileInfo.NOT_AVAILABLE);
							table.getModel().setValueAt(FileInfo.NOT_AVAILABLE,
									row, 7);

							continue;
						}

						value = (String) json.get("Genre");
						Search.setFileAttribute(absPath, FileInfo.ATTR_GENRE,
								value);
						table.getModel().setValueAt(value, row, 6);

						value = (String) json.get("imdbRating");
						Search.setFileAttribute(absPath, FileInfo.ATTR_RATING,
								value);
						table.getModel().setValueAt(value, row, 7);

						Search.setFileAttribute(absPath,
								FileInfo.ATTR_IMDB_DONE, FileInfo.DONE);

					} catch (Exception e1) {
						// ignore and attempt next
					}
				}
				frmMovLib.setCursor(Cursor.getDefaultCursor());
			}
		});

		frmMovLib.getContentPane().add(imdbButton);

	}

	@SuppressWarnings("unchecked")
	private void initFileTypeList() {

		final DefaultListModel<String> model = new DefaultListModel<String>();

		model.addElement("AVI");
		model.addElement("VOB");
		model.addElement("mkv");

		fileTypelist = new JList(model) {

			/**
			 * 
			 */
			private static final long serialVersionUID = -6301730656624777789L;

			@Override
			public int getScrollableUnitIncrement(Rectangle visibleRect,
					int orientation, int direction) {
				int row;
				if (orientation == SwingConstants.VERTICAL && direction < 0
						&& (row = getFirstVisibleIndex()) != -1) {
					Rectangle r = getCellBounds(row, row);
					if ((r.y == visibleRect.y) && (row != 0)) {
						Point loc = r.getLocation();
						loc.y--;
						int prevIndex = locationToIndex(loc);
						Rectangle prevR = getCellBounds(prevIndex, prevIndex);

						if (prevR == null || prevR.y >= r.y) {
							return 0;
						}
						return prevR.height;
					}
				}
				return super.getScrollableUnitIncrement(visibleRect,
						orientation, direction);
			}
		};

		final JTextField input = new JTextField(6);
		input.setToolTipText("To add more in this list, type extension and hit ENTER");
		input.setPreferredSize(new Dimension(70, 20));
		input.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent aActionEvent) {
				String text = input.getText();
				if (text.length() > 0) {
					model.addElement(text);
					input.setText("");
				}
			}
		});

		JScrollPane listScroller = new JScrollPane(fileTypelist);
		listScroller.setPreferredSize(new Dimension(80, 57));
		// listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel listPane = new JPanel();
		listPane.setBounds(264, 27, 100, 80);
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));

		listPane.add(listScroller);
		listPane.add(input);

		frmMovLib.getContentPane().add(listPane);
	}

	/**
	 * 
	 */
	private void initSearchButton() {
		btnSearch = new JButton("Search");
		btnSearch.setBounds(572, 81, 89, 23);
		frmMovLib.getContentPane().add(btnSearch);

		btnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					frmMovLib.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));

					table.setRowSorter(null);
					resetModel();

					List<String> exts = fileTypelist.getSelectedValuesList();
					List<String> dirs = driveList.getSelectedValuesList();

					Collection<FileInfo> files = Search.search(dirs, exts);

					if (null == files) {
						noOfRec.setText("0");
						return;
					}

					for (FileInfo f : files) {

						model.addRow(createResultRow(f));
					}

					table.setRowSorter(new TableRowSorter<DefaultTableModel>(
							model));
					noOfRec.setText(Integer.toString(files.size()));
				} catch (Exception e1) {
					showErrorMessage("Please select and search again.");
				}
				frmMovLib.setCursor(Cursor.getDefaultCursor());

			}
		});
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void initDriveList() {

		DefaultListModel<String> model = new DefaultListModel<String>();

		for (File d : File.listRoots()) {

			if (!d.exists() || !d.isDirectory()) {
				continue;
			}
			model.addElement(d.getAbsolutePath());
		}

		driveList = new JList(model) {

			@Override
			public int getScrollableUnitIncrement(Rectangle visibleRect,
					int orientation, int direction) {
				int row;
				if (orientation == SwingConstants.VERTICAL && direction < 0
						&& (row = getFirstVisibleIndex()) != -1) {
					Rectangle r = getCellBounds(row, row);
					if ((r.y == visibleRect.y) && (row != 0)) {
						Point loc = r.getLocation();
						loc.y--;
						int prevIndex = locationToIndex(loc);
						Rectangle prevR = getCellBounds(prevIndex, prevIndex);

						if (prevR == null || prevR.y >= r.y) {
							return 0;
						}
						return prevR.height;
					}
				}
				return super.getScrollableUnitIncrement(visibleRect,
						orientation, direction);
			}
		};

		JScrollPane listScroller = new JScrollPane(driveList);
		listScroller.setPreferredSize(new Dimension(80, 57));
		listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel listPane = new JPanel();
		listPane.setBounds(70, 27, 80, 57);
		frmMovLib.getContentPane().add(listPane);
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));

		listPane.add(listScroller);

	}

	/**
	 * 
	 */
	private void initTable() {

		model = new DefaultTableModel(columnNames, 0) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4397910273310072344L;

			@Override
			public boolean isCellEditable(int row, int column) {

				if (1 == column) {
					return true;
				}

				return false;
			}
		};

		table = new JTable(model);
		table.setToolTipText("Right click on any row for more options");
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// addTableDoubleClickListener();

		addTablePopupMenu();

		Action action = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -580208378121965061L;

			@Override
			public void actionPerformed(ActionEvent e) {
				TableCellListener tcl = (TableCellListener) e.getSource();
				int viewRow = table.getSelectedRow();
				int row = table.convertRowIndexToModel(viewRow);
				String absPath = (String) table.getModel().getValueAt(row, 0);
				int column = tcl.getColumn();
				String newValue = ((String) tcl.getNewValue());

				if ("".equals(newValue.trim())) {
					table.setValueAt(tcl.getOldValue(), row, column);
				} else {
					Search.setFileAttribute(absPath, FileInfo.ATTR_TITLE,
							newValue);
					Search.setFileAttribute(absPath, FileInfo.ATTR_IMDB_DONE,
							FileInfo.NOT_AVAILABLE);
				}
			}
		};

		TableCellListener tcl = new TableCellListener(table, action);

		table.getTableHeader().setReorderingAllowed(false);
		table.setBounds(10, 122, 759, 296);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(table);
		scrollPane.setBounds(10, 122, 759, 367);

		Dimension tableSize = scrollPane.getPreferredSize();
		setColumnsWidth(tableSize);

		frmMovLib.getContentPane().add(scrollPane);

	}

	/**
	 * @param scrollPane
	 */
	private void setColumnsWidth(Dimension tableSize) {

		table.getColumnModel().getColumn(0)
				.setPreferredWidth(Math.round(tableSize.width * 0.20f));
		table.getColumnModel().getColumn(1)
				.setPreferredWidth(Math.round(tableSize.width * 0.40f));
		table.getColumnModel().getColumn(2)
				.setPreferredWidth(Math.round(tableSize.width * 0.10f));
		table.getColumnModel().getColumn(3)
				.setPreferredWidth(Math.round(tableSize.width * 0.10f));
		table.getColumnModel().getColumn(4)
				.setPreferredWidth(Math.round(tableSize.width * 0.10f));
		table.getColumnModel().getColumn(5)
				.setPreferredWidth(Math.round(tableSize.width * 0.10f));
		table.getColumnModel().getColumn(6)
				.setPreferredWidth(Math.round(tableSize.width * 0.20f));
		table.getColumnModel().getColumn(7)
				.setPreferredWidth(Math.round(tableSize.width * 0.06f));
	}

	/**
	 * 
	 */
	private void addTablePopupMenu() {
		final JPopupMenu popupMenu = new JPopupMenu();

		JMenuItem openFolder = new JMenuItem("Open Folder Location");
		openFolder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int viewRow = table.getSelectedRow();
					int row = table.convertRowIndexToModel(viewRow);
					String absPath = (String) table.getModel().getValueAt(row,
							0);

					String cmd = "explorer.exe /select," + absPath;
					Runtime.getRuntime().exec(cmd);
				} catch (Exception e1) {
					showErrorMessage("Select a row and try again. If error persists contact developer");
				}
			}
		});

		JMenuItem toggleMovieSeen = new JMenuItem("Toggle - Movie Seen");
		toggleMovieSeen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					int viewRow = table.getSelectedRow();
					int row = table.convertRowIndexToModel(viewRow);

					String movieSeen = ((String) (table.getModel().getValueAt(
							row, 5))).equals(FileInfo.YES) ? FileInfo.NO
							: FileInfo.YES;

					String fileAbsPath = (String) table.getModel().getValueAt(
							row, 0);
					String attribute = FileInfo.ATTR_MOVIE_SEEN;

					boolean success = Search.setFileAttribute(
							fileAbsPath.toString(), attribute, movieSeen);

					if (success) {
						JOptionPane.showMessageDialog(
								frmMovLib,
								"Marked Movie Seen as \""
										+ movieSeen
										+ "\" for "
										+ (String) table.getModel().getValueAt(
												row, 1));
						table.getModel().setValueAt(movieSeen, row, 5);
					} else {
						showErrorMessage("Select a row and try again. If error persists contact developer");
					}
				} catch (Exception e1) {
					showErrorMessage("Select a row and try again. If error persists contact developer");
				}

			}

		});

		popupMenu.add(openFolder);
		popupMenu.add(toggleMovieSeen);
		table.setComponentPopupMenu(popupMenu);
	}

	/**
	 * @param string
	 * 
	 */
	private void showErrorMessage(String msg) {

		String str = "An error occured. ";

		if (msg != null && msg.length() > 0) {
			str = str + msg;
		}

		JOptionPane.showMessageDialog(frmMovLib, str);
	}

	/**
	 * 
	 */
	private void resetModel() {
		model.setRowCount(0);
	}

	/**
	 * @param f
	 * @return
	 */
	private Object[] createResultRow(FileInfo f) {

		// "File Name", "Title", "Size", "Create Date", "File Type",
		// "Movie Seen", "Genre"

		return new Object[] { f.getFileName(), f.getTitle(), f.getSize(),
				f.getCreateDate(), f.getFileType(), f.getMovieSeen(),
				f.getGenre(), f.getRating() };
	}
}
