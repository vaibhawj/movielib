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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
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

public class AppWindow {

	private JFrame frmMovLib;
	private JList driveList;
	private JList fileTypelist;
	private JLabel lblRoots;
	private JButton btnSearch;
	private JTable table;
	private DefaultTableModel model;

	private static final Object[] columnNames = new Object[] { "Location",
			"File Name", "Size", "Create Date", "File Type", "Movie Seen" };
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
		} catch (IOException e) {
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

				return false;
			}
		};

		table = new JTable(model);
		table.setToolTipText("Right click on any row for more options");
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// addTableDoubleClickListener();

		addTablePopupMenu();

		table.setBounds(10, 122, 759, 296);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(table);
		scrollPane.setBounds(10, 122, 759, 367);
		frmMovLib.getContentPane().add(scrollPane);

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
					StringBuilder absPath = new StringBuilder((String) table
							.getModel().getValueAt(row, 0)).append(
							FileSystems.getDefault().getSeparator()).append(
							(String) table.getModel().getValueAt(row, 1));

					String cmd = "explorer.exe /select," + absPath.toString();
					Runtime.getRuntime().exec(cmd);
				} catch (Exception e1) {
					showErrorMessage(null);
				}
			}
		});

		JMenuItem toggleMovieSeen = new JMenuItem("Toggle - Movie Seen");
		toggleMovieSeen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				int viewRow = table.getSelectedRow();
				int row = table.convertRowIndexToModel(viewRow);

				String movieSeen = ((String) (table.getModel().getValueAt(row,
						5))).equals(FileInfo.MOVIE_SEEN) ? FileInfo.MOVIE_NOT_SEEN
						: FileInfo.MOVIE_SEEN;

				StringBuilder absPath = new StringBuilder((String) table
						.getModel().getValueAt(row, 0)).append(
						FileSystems.getDefault().getSeparator()).append(
						(String) table.getModel().getValueAt(row, 1));

				File f = new File(absPath.toString());

				UserDefinedFileAttributeView view = Files.getFileAttributeView(
						f.toPath(), UserDefinedFileAttributeView.class);

				boolean success = true;
				try {
					view.write("user.movieseen", Charset.defaultCharset()
							.encode(movieSeen));
				} catch (Exception e1) {
					success = false;
				}

				if (success) {
					JOptionPane.showMessageDialog(
							frmMovLib,
							"Marked Movie Seen as \""
									+ movieSeen
									+ "\" for "
									+ (String) table.getModel().getValueAt(row,
											1));
					table.getModel().setValueAt(movieSeen, row, 5);
				} else {
					showErrorMessage(null);
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
	private void addTableDoubleClickListener() {
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable target = (JTable) e.getSource();
					int viewRow = table.getSelectedRow();

					int row = table.convertRowIndexToModel(viewRow);
					// int column = target.getSelectedColumn();

					StringBuilder absPath = new StringBuilder((String) table
							.getModel().getValueAt(row, 0)).append(
							FileSystems.getDefault().getSeparator()).append(
							(String) table.getModel().getValueAt(row, 1));

					JOptionPane.showMessageDialog(frmMovLib, absPath.toString());
				}
			}
		});
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

		// "Location", "File Name", "Size", "Create Date",
		// "File Type"

		return new Object[] { f.getLocation(), f.getFileName(), f.getSize(),
				f.getCreateDate(), f.getFileType(), f.getMovieSeen() };
	}
}
