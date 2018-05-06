import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class HostClass extends JPanel {
	private static final long serialVersionUID = 1L;

	JFrame frame;
	public static String barTitle = "Adox Dashboard Installer";

	public HostClass() {

		frame = new JFrame();

		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {

			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
				if (!oldFileFound || !newFileFound || !configFileFound)
					System.exit(0);
			}

			public void mouseExited(MouseEvent e) {
			}
		});

		setFocusable(true);
	}

	public static String getRunningJARFolder() {
		try {
			try {
				return new File(HostClass.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (URISyntaxException e) {}
		return null;
	}

	boolean oldFileFound, configFileFound, newFileFound;

	static File configFile, oldFile, updateFile;

	public void begin() {
		if ((configFile = new File("C:\\Adox\\tmp\\jarfile.updateconfig")).exists()) {
			configFileFound = true;
			try {
				BufferedReader r = new BufferedReader(new FileReader(configFile));

				String s = r.readLine();
				if (s != null && (oldFile = new File(s)).exists()) {
					oldFileFound = true;
				}
				r.close();

			} catch (IOException e) {}
		}
		if ((updateFile = new File("C:\\Adox\\tmp\\AdoxUpdate.jar")).exists()) {
			newFileFound = true;
		}
		if (newFileFound && oldFileFound) {
			configFile.delete();
			oldFile.delete();
			updateFile.renameTo(oldFile);
		}
	}

	public void finish() {
		try {
			Runtime.getRuntime().exec("java -jar \"" + oldFile.getCanonicalPath() + "\"");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	boolean startUpdate = true;

	Color bg = new Color(0, 0, 0);

	public void paint(Graphics gr) {
		super.paint(gr);
		Graphics2D g = (Graphics2D) gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.red.darker().darker().darker());
		g.fillRect(0, 0, getWidth(), getHeight());

		g.setFont(new Font("Arial", Font.PLAIN, 15));
		g.setColor(Color.white);

		FontMetrics FM = g.getFontMetrics();
		g.drawString("An error occurred during installation", frame.getWidth() / 2 - FM.stringWidth("An error occurred during installation") / 2, frame.getHeight() / 2 - 20);

		String s = "for an unknown reason.";
		if (!configFileFound) {
			s = "because the configuration file could not be found.";
		} else if (!oldFileFound) {
			try {
				if (oldFile == null)
					s = "because the configuration file is corrupt.";
				else
					s = "because the file at \"" + oldFile.getCanonicalPath() + "\" is nonexistent.";
			} catch (IOException e) {}
		} else if (!newFileFound) {
			s = "because the update file has gone missing.";
		}
		if (FM.stringWidth(s)>500) {
			frame.setSize(FM.stringWidth(s) + 50, 300);
			frame.setLocationRelativeTo(null);
		}
		g.drawString(s, frame.getWidth() / 2 - FM.stringWidth(s) / 2, frame.getHeight() / 2);

		g.drawString("Click to abort.", frame.getWidth() / 2 - FM.stringWidth("Click to abort.") / 2, frame.getHeight() * (3f / 4f));

	}

	public void go() {
		long lastTime = System.nanoTime();
		final double ns = 1_000_000_000.0 / 15.0;
		double delta = 0;
		begin();
		frame.setVisible(true);
		while (!oldFileFound || !newFileFound) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				repaint();
				delta--;
			}
		}
		finish();
	}

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (updateFile != null && updateFile.exists())
					updateFile.delete();
			}
		});

		HostClass host = new HostClass();
		host.frame.setTitle(HostClass.barTitle);
		host.frame.add(host);
		host.frame.setSize(500, 300);
		host.frame.setUndecorated(true);
		host.frame.setLocationRelativeTo(null);
		host.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		host.frame.setResizable(false);
		host.go();
	}
}