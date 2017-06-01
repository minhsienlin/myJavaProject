package uistuff;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.MemoryImageSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


/**
 * This class provides some static methods to view different styles of content.
 * Every call to a method creates a frame viewing a simple representation of the
 * provided content.
 * 
 * @author Volker Christian (volker.christian@fh-hagenberg.at)
 * @version 1.0
 */
public class Presenter {
	static private class HistogramFrame extends Frame {
		private static final long serialVersionUID = -1297885049512116682L;

		private Vector<Integer> histogramLengths = new Vector<Integer>();

		private Vector<int[]> histograms = new Vector<int[]>();

		private Vector<Color> histogramColors = new Vector<Color>();

		private int maxHeight = Integer.MIN_VALUE;

		private int maxWidth = Integer.MIN_VALUE;

		private Color backgroundColor = Color.WHITE;

		private HistogramCanvas histogramCanvas = null;

		private class HistogramFrameWindowAdapter extends WindowAdapter {
			private HistogramFrame histogramFrame = null;

			HistogramFrameWindowAdapter(HistogramFrame hf) {
				this.histogramFrame = hf;
			}

			public void windowClosing(WindowEvent e) {
				histogramFrame.closeFrame();
			}
		}

		private class HistogramCanvas extends Canvas {
			private static final long serialVersionUID = 5573564144397035012L;

			private Image offScreenImage = null;

			private Graphics offScreenGraphics = null;

			private int oldWidth = Integer.MIN_VALUE;

			private int oldHeight = Integer.MIN_VALUE;

			public void paintHistogram(Graphics g) {
				Dimension d = this.getSize();
				int textHeight = g.getFontMetrics().getHeight();
				int textWidth = 10;
				int histogrammXOffset = textWidth;
				int histogrammYOffset = 0;
				int histogrammWidth = d.width - textWidth;
				int histogrammHeight = d.height - textHeight;
				g.setColor(backgroundColor);
				g.fillRect(0, 0, histogrammWidth, histogrammHeight);

				double factorY = (histogrammHeight) / (double) maxHeight;
				double factorX = (histogrammWidth) / (double) (maxWidth);

				for (int h = 0; h < histograms.size(); h++) {
					int red = histogramColors.get(h).getRed();
					int green = histogramColors.get(h).getGreen();
					int blue = histogramColors.get(h).getBlue();
					Color lineColor = new Color(red, green, blue);
					int[] histogram = histograms.elementAt(h);
					int yO = histogrammYOffset;
					int xO = histogrammXOffset;
					for (int i = 0; i < histogramLengths.get(h).intValue(); i++) {
						int xN = histogrammXOffset + (int) ((i + 1) * factorX);
						int yN = histogrammYOffset + histogrammHeight
								- (int) (((double) histogram[i]) * factorY);

						g.setColor(lineColor);
						g.drawLine(xO, yO, xO, yN);
						g.drawLine(xO, yN, xN, yN);

						g.setColor((Color) histogramColors.get(h));
						int[] xPoints = { xO, xO, xN, xN };
						int[] yPoints = { histogrammHeight, yN, yN,
								histogrammHeight };
						g.fillPolygon(xPoints, yPoints, 4);

						xO = xN;
						yO = yN;
					}
				}
				if (histograms.size() > 0) {
					g.setColor(Color.BLACK);
					g.drawLine(histogrammXOffset, histogrammHeight,
							histogrammXOffset + (int) (maxWidth * factorX),
							histogrammHeight);
					g.drawLine(histogrammXOffset, histogrammHeight,
							histogrammXOffset, 0);
					int fontHeight = g.getFontMetrics().getHeight();
					int numberOfLabels = 6;
					for (int i = 0; i <= numberOfLabels; i++) {
						int dx = (int) (Math
								.round(((maxWidth - 1.0) / numberOfLabels) * i)
								* factorX + 0.5 * factorX);
						String s = String.valueOf(Math.round(i
								* (maxWidth - 1.0) / numberOfLabels));
						g.drawString(s, histogrammXOffset
								- g.getFontMetrics().stringWidth(s) + dx,
								histogrammHeight + fontHeight);
						g.drawLine(histogrammXOffset + dx, histogrammHeight,
								histogrammXOffset + dx, histogrammHeight
										+ fontHeight);
					}
				}
			}

			public void paintOffScreenHistogram(Graphics g, boolean forceRepaint) {
				int width = getSize().width;
				int height = getSize().height;
				if (oldWidth != width || oldHeight != height) {
					oldWidth = width;
					oldHeight = height;
					offScreenImage = createImage(width, height);
					offScreenGraphics = offScreenImage.getGraphics();
					forceRepaint = true;
				}

				if (forceRepaint) {
					paintHistogram(offScreenGraphics);
				}

				width = getSize().width;
				height = getSize().height;
				if (oldWidth == width && oldHeight == height) {
					g.drawImage(offScreenImage, 0, 0, this);
				}
			}

			public void update(Graphics g) {
				/*
				 * Called by repaint such an update-call should force a complete
				 * repaint
				 */
				paintOffScreenHistogram(g, true);
			}

			public void paint(Graphics g) {
				/*
				 * Called by the system * again visible * resize of the frame
				 * such an paint-call should lead to a selective forced repaint.
				 * 
				 * No complete repaint should happen if the frame hasn't changed
				 * size. In this case it is sufficient to map the already filled
				 * offscreen-image to the Graphics g
				 * 
				 * A complete repaint should happen if the frame has changed
				 * size
				 */
				paintOffScreenHistogram(g, false);
			}
		}

		protected HistogramFrame(String title) {
			this.setResizable(false);
			this.setTitle("Histogram - " + title);
			this.addWindowListener(new HistogramFrameWindowAdapter(this));
			this.add(histogramCanvas = new HistogramCanvas());
			histogramCanvas.setPreferredSize(new Dimension(260, 150));
			// histogramCanvas.setPreferredSize(new Dimension(390, 225));
		}

		protected void addHistogram(int[] histogram, int length, int max,
				Color color) {
			histograms.add(histogram);
			this.histogramLengths.add(new Integer(length));
			histogramColors.add(color);
			this.maxHeight = (this.maxHeight < max) ? max : this.maxHeight;
			this.maxWidth = (this.maxWidth < length) ? length : this.maxWidth;
			histogramCanvas.repaint();
		}

		private void closeFrame() {
			dispose();
		}

		protected void setBackgroundColor(Color c) {
			backgroundColor = c;
		}
	}

	/**
	 * This method creates a frame and draws a couble of histograms in it.
	 * 
	 * @param title
	 *            the title showing up in the title bar of the frame
	 * @param histograms
	 *            a two-dimensional array containing a couble of histograms. The
	 *            first index selects a specific histogram, the second index
	 *            numbers all values of a specific histogram selected by the
	 *            first index.
	 * @param colors
	 *            the colors used for the histograms. The index selects a
	 *            specific color for the corresponding specific histogram.
	 */
	public static void viewHistogram(String title, int[][] histograms,
			Color[] colors, Color backGroundColor) {
		HistogramFrame histogramFrame = new HistogramFrame(title);
		histogramFrame.setBackground(backGroundColor);
		histogramFrame.setBackgroundColor(backGroundColor);
		histogramFrame.pack();
		histogramFrame.setVisible(true);

		for (int h = 0; h < histograms.length; h++) {
			int max = Integer.MIN_VALUE;
			int[] histogram = histograms[h];
			Color color = colors[h];
			for (int i = 0; i < histogram.length; i++) {
				max = (max < histogram[i]) ? histogram[i] : max;
			}
			histogramFrame
					.addHistogram(histogram, histogram.length, max, color);
		}
	}

	/**
	 * This method creates a frame and draws a histogram in it.
	 * 
	 * @param title
	 *            the title showing up in the title bar of the frame
	 * @param histogram
	 *            an integer array containing the histogram information
	 */
	public static void viewHistogram(String title, int[] histogram) {
		viewHistogram(title, histogram, Color.BLACK, Color.WHITE);
	}

	/**
	 * This method creates a frame and draws a histogram in it.
	 * 
	 * @param title
	 *            the title showing up in the title bar of the frame
	 * @param histogram
	 *            an integer array containing the histogram information
	 * @param foreGroundColor
	 *            the color used for the histogram
	 */
	public static void viewHistogram(String title, int[] histogram,
			Color foreGroundColor) {
		viewHistogram(title, histogram, foreGroundColor, Color.WHITE);
	}

	/**
	 * This method creates a frame and draws a histogram in it.
	 * 
	 * @param title
	 *            the title showing up in the title bar of the frame
	 * @param histogram
	 *            an integer array containing the histogram information
	 * @param foreGroundColor
	 *            the color used for the histogram
	 * @param backGroundColor
	 *            the background color of the histogram
	 */
	public static void viewHistogram(String title, int[] histogram,
			Color foreGroundColor, Color backGroundColor) {

		int[][] histograms = new int[1][];

		histograms[0] = histogram;

		Color[] colors = new Color[1];
		colors[0] = foreGroundColor;

		viewHistogram(title, histograms, colors, backGroundColor);
	}

	/**
	 * This method creates a frame and draws three histograms in it.
	 * 
	 * @param title
	 *            the title showing up in the title bar of the frame
	 * @param histogram1
	 *            an integer array containing the first histogram information
	 * @param color1
	 *            the color used for the first histogram
	 * @param histogram2
	 *            an integer array containing the second histogram information
	 * @param color2
	 *            the color used for the second histogram
	 * @param histogram3
	 *            an integer array containing the third histogram information
	 * @param color3
	 *            the color used for the third histogram
	 */
	public static void viewHistogram(String title, int[] histogram1,
			Color color1, int[] histogram2, Color color2, int[] histogram3,
			Color color3) {
		viewHistogram(title, histogram1, color1, histogram2, color2,
				histogram3, color3, Color.WHITE);
	}

	/**
	 * This method creates a frame and draws three histograms in it.
	 * 
	 * @param title
	 *            the title showing up in the title bar of the frame
	 * @param histogram1
	 *            an integer array containing the first histogram information
	 * @param color1
	 *            the color used for the first histogram
	 * @param histogram2
	 *            an integer array containing the second histogram information
	 * @param color2
	 *            the color used for the second histogram
	 * @param histogram3
	 *            an integer array containing the third histogram information
	 * @param color3
	 *            the color used for the third histogram
	 * @param backGroundColor
	 *            the background color of the histograms
	 */
	public static void viewHistogram(String title, int[] histogram1,
			Color color1, int[] histogram2, Color color2, int[] histogram3,
			Color color3, Color backGroundColor) {

		int[][] histograms = new int[3][];
		histograms[0] = histogram1;
		histograms[1] = histogram2;
		histograms[2] = histogram3;

		Color[] colors = new Color[3];
		colors[0] = color1;
		colors[1] = color2;
		colors[2] = color3;

		viewHistogram(title, histograms, colors, backGroundColor);
	}

	/* ---------------------- Image methods ------------------------ */
	static private class ImageFrame extends Frame {
		private static final long serialVersionUID = -3961613485615270343L;

		private ImageCanvas imageCanvas = null;

		private Image image = null;

		private class ImageFrameWindowAdapter extends WindowAdapter {
			private ImageFrame imageFrame = null;

			ImageFrameWindowAdapter(ImageFrame imageFrame) {
				this.imageFrame = imageFrame;
			}

			public void windowClosing(WindowEvent e) {
				imageFrame.closeFrame();
			}
		}

		private class ImageCanvas extends Canvas {
			private static final long serialVersionUID = 2481133924901847011L;

			public void paint(Graphics g) {
				g.drawImage(image, 0, 0, this);
			}
		}

		public ImageFrame(Image image) {
			this.addWindowListener(new ImageFrameWindowAdapter(this));
			this.setLayout(new BorderLayout());
			this.image = image;
			imageCanvas = new ImageCanvas();
			this.add(imageCanvas);
		}

		public void setCanvasSize(int width, int height) {
			imageCanvas.setPreferredSize(new Dimension(width, height));
		}

		private void closeFrame() {
			dispose();
		}
	}

	/**
	 * The method viewImage opens a frame and displays a RGB-Image in it.
	 * 
	 * @param title
	 *            the title showing up in the title bar of the frame
	 * @param rgbImage
	 *            a three-dimensional array containing the RGB-data of the
	 *            image. The first index is the y-coordinate into the image, the
	 *            second index is the x-coordinate into the image and the third
	 *            index selects a color-component: red = 0, green = 1, and blue =
	 *            2
	 */
	public static void viewImage(String title, int[][][] rgbImage) {
		int[] pixels = new int[rgbImage[0].length * rgbImage.length];
		int index = 0;

		for (int y = 0; y < rgbImage.length; y++) {
			for (int x = 0; x < rgbImage[0].length; x++) {
				int color = 255 << 24;
				for (int c = 0; c < 3; c++) {
					color |= rgbImage[y][x][c] << (8 * (2 - c));
				}
				pixels[index++] = color;
			}
		}
		Image image = Toolkit.getDefaultToolkit().createImage(
				new MemoryImageSource(rgbImage[0].length, rgbImage.length,
						pixels, 0, rgbImage[0].length));

		ImageFrame imf = new ImageFrame(image);
		imf.setTitle("Image - " + title);
		/* ---------------------- Image methods ------------------------ */
		imf.setCanvasSize(rgbImage[0].length, rgbImage.length);
		imf.pack();
		imf.setResizable(false);
		imf.setVisible(true);
	}

	/**
	 * The method viewImage opens a frame and displays a RGB-Image in it.
	 * 
	 * @param title
	 *            the title showing up in the title bar of the frame
	 * @param grayScaleImage
	 *            a two-dimensional array containing the luminance-data of the
	 *            image. The first index is the y-coordinate into the image, the
	 *            second index is the x-coordinate into the image.
	 */
	public static void viewImage(String title, int[][] grayScaleImage) {
		int[][][] rgbImage = new int[grayScaleImage.length][grayScaleImage[0].length][3];

		for (int y = 0; y < grayScaleImage.length; y++) {
			for (int x = 0; x < grayScaleImage[0].length; x++) {
				rgbImage[y][x] = ImageGrabber.ImageGrabberUtilities
						.convertYCbCrToRGB(
								(double) grayScaleImage[y][x] / 256.0, 0.5, 0.5);
			}
		}
		viewImage(title, rgbImage);
	}

	/* ---------------------- Movie methods ------------------------ */
	static class MovieFrame extends Frame {
		private static final long serialVersionUID = 2468836318900893949L;

		private MovieCanvas movieCanvas = null;

		private Image image = null;
		
		private int[][][] rgbImage = null;
		
		private int[][] grayScaleImage = null;

		private class MovieFrameWindowAdapter extends WindowAdapter {
			private MovieFrame movieFrame = null;

			MovieFrameWindowAdapter(MovieFrame movieFrame) {
				this.movieFrame = movieFrame;
			}

			public void windowClosing(WindowEvent e) {
				if (grayScaleImage != null) {
					movieFrames.remove(grayScaleImage);
				} else {
					movieFrames.remove(rgbImage);
				}
				movieFrame.closeFrame();
			}
		}

		private class MovieCanvas extends Canvas {
			static final long serialVersionUID = -3517264303987741422L;
			
			public void update(Graphics g) {
				paint(g);
			}
			
			public void paint(Graphics g) {
				g.drawImage(image, 0, 0, this);
			}
		}

		public MovieFrame(String title, int[][][] rgbImage) {
			this.rgbImage = rgbImage;
			initFrame(title);
		}
		
		public MovieFrame(String title, int[][] grayScaleImage) {
			this.grayScaleImage = grayScaleImage;
			rgbImage = new int[grayScaleImage.length][grayScaleImage[0].length][3];
			initFrame(title);
		}

		private void initFrame(String title) {
			this.addWindowListener(new MovieFrameWindowAdapter(this));
			this.setLayout(new BorderLayout());
			this.image = createImage();
			movieCanvas = new MovieCanvas();
			this.add(movieCanvas);
			this.setTitle("Movie - " + title);
			this.setCanvasSize(rgbImage[0].length, rgbImage.length);
			this.pack();
			this.setResizable(false);
			this.setVisible(true);
		}

		private Image createImage() {
			if (grayScaleImage != null) {
				for (int y = 0; y < grayScaleImage.length; y++) {
					for (int x = 0; x < grayScaleImage[0].length; x++) {
						rgbImage[y][x] = ImageGrabber.ImageGrabberUtilities
						.convertYCbCrToRGB(
								(double) grayScaleImage[y][x] / 256.0, 0.5, 0.5);
					}
				}
			}
			
			int[] pixels = new int[rgbImage[0].length * rgbImage.length];
			int index = 0;

			for (int y = 0; y < rgbImage.length; y++) {
				for (int x = 0; x < rgbImage[0].length; x++) {
					int color = 255 << 24;
					for (int c = 0; c < 3; c++) {
						color |= rgbImage[y][x][c] << (8 * (2 - c));
					}
					pixels[index++] = color;
				}
			}
			
			Image image = Toolkit.getDefaultToolkit().createImage(
					new MemoryImageSource(rgbImage[0].length, rgbImage.length,
							pixels, 0, rgbImage[0].length));
			
			return image;
		}

		public void updateCanvas() {
			this.image = createImage();
			movieCanvas.repaint();
		}

		public void setCanvasSize(int width, int height) {
			movieCanvas.setPreferredSize(new Dimension(width, height));
		}

		private void closeFrame() {
			dispose();
		}
	}

	static Map<Object, MovieFrame> movieFrames = new HashMap<Object, MovieFrame>();
	
	public static MovieFrame viewMovie(String title, int[][][] rgbImage) {
		MovieFrame mf = null;
		if (movieFrames.containsKey(rgbImage)) {
			mf = updateMovie(rgbImage);
		} else {
			mf = new MovieFrame(title, rgbImage);
			movieFrames.put(rgbImage, mf);
		}	
		return mf;
	}
	
	public static MovieFrame viewMovie(String title, int[][] grayScaleImage) {
		MovieFrame mf = null;
		if (movieFrames.containsKey(grayScaleImage)) {
			mf = updateMovie(grayScaleImage);
		} else {
			mf = new MovieFrame(title, grayScaleImage);
			movieFrames.put(grayScaleImage, mf);
		}
		return mf;
	}
	
	public static MovieFrame updateMovie(MovieFrame mf) {
		mf.updateCanvas();
		return mf;
	}
	
	public static MovieFrame updateMovie(int[][][] rgbImage) {
		MovieFrame mf = null;
		if (movieFrames.containsKey(rgbImage)) {
			mf = movieFrames.get(rgbImage);
			updateMovie(mf);
		}
		return mf;
	}
	
	public static MovieFrame updateMovie(int[][] grayScaleImage) {
		MovieFrame mf = null;
		if (movieFrames.containsKey(grayScaleImage)) {
			mf = movieFrames.get(grayScaleImage);
			updateMovie(mf);
		}
		return mf;
	}
}
