package uistuff;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * This class provides two static methods for grabbing pixel-information of
 * images and
 * 
 * The first one grabGrayScale(String path) converts the image to gray-scale and
 * returns the gray-scale value of the pixel in a two-dimensional array. The
 * second one, grabRGB(String path) retrieves all three color-components of the
 * pixels of the image and returns this information in a three-dimensional
 * array.
 * 
 * Two static methods for color-space conversion between the RGB and the YCrCb
 * colorspaces are also provided.
 * 
 * @author Volker Christian (volker.christian@fh-hagenberg.at)
 * @version 1.0
 */
public class ImageGrabber {
	static class ImageGrabberUtilities {
		private ImageByteArrayObserver imageObserver;
		private Image image;
		private int width;
		private int height;

		private class ImageByteArrayObserver implements ImageObserver {
			private boolean errored = false;
			private ImageGrabberUtilities igu = null;

			public ImageByteArrayObserver(ImageGrabberUtilities mioi) {
				igu = mioi;
			}

			public boolean imageUpdate(Image img, int infoflags, int x, int y,
					int width, int height) {

				if ((infoflags & (ERROR)) != 0) {
					errored = true;
				}
				if ((infoflags & (WIDTH | HEIGHT)) != 0) {
					if (igu != null) {
						igu.sizeKnown();
					}
				}
				boolean done = ((infoflags & (ERROR | FRAMEBITS | ALLBITS)) != 0);

				return !done;
			}

			public boolean isErrored() {
				return errored;
			}
		}

		
		private ImageGrabberUtilities() {
		}

		
		private static int[] convertToRGB(int pixel) {
			int[] RGB = new int[3];
			// Alpha = (pixel >> 24) & 0xff;
			RGB[0] = (int) ((pixel >> 16) & 0xff);
			RGB[1] = (int) ((pixel >> 8) & 0xff);
			RGB[2] = (int) ((pixel) & 0xff);

			return RGB;
		}

		private static double[] convertToYCbCr(int pixel) {
			int[] rgb = convertToRGB(pixel);
			return convertRGBToYCbCr(rgb[0], rgb[1], rgb[2]);
		}

		/**
		 * Transforms the YCbCr color-space into the RGB color-space
		 * 
		 * @param Y
		 *            the luminance value
		 * @param Cb
		 *            the first croma value
		 * @param Cr
		 *            the second croma value
		 * @return a integer array containing the three RGB components. The
		 *         components have the following indices: red = 0; green = 1;
		 *         blue = 2
		 */
		protected static int[] convertYCbCrToRGB(double Y, double Cb, double Cr) {
			double R = Y + 701.0 / 500.0 * (Cr - 1.0 / 2.0);
			double G = Y - 25251.0 / 73375.0 * (Cb - 1.0 / 2.0) - 209599.0
					/ 293500.0 * (Cr - 1.0 / 2.0);
			double B = Y + 443.0 / 250.0 * (Cb - 1.0 / 2.0);

			int[] RGB = new int[3];

			RGB[0] = (int) (R * 256);
			RGB[1] = (int) (G * 256);
			RGB[2] = (int) (B * 256);

			return RGB;
		}

		/**
		 * Transforms the RGB color-space into the YCrCb color-space
		 * 
		 * @param r
		 *            the red component
		 * @param g
		 *            the green component
		 * @param b
		 *            the blue component
		 * @return a integer array containing the three YCrCb components. The
		 *         components have the following indices: Y = 0; Cr = 1; Cb = 2
		 */
		protected static double[] convertRGBToYCbCr(int r, int g, int b) {
			double R = ((double) r) / 256.0;
			double G = ((double) g) / 256.0;
			double B = ((double) b) / 256.0;

			double Y = 0.299 * R + 0.587 * G + 0.114 * B;
			double Cb = (B - Y) / 1.772 + 0.5;
			double Cr = (R - Y) / 1.402 + 0.5;

			double[] YCbCr = new double[3];

			YCbCr[0] = Y;
			YCbCr[1] = Cb;
			YCbCr[2] = Cr;

			return YCbCr;
		}

		private int[] grabPixels() throws InterruptedException {
			int[] pixels = null;

			width = image.getWidth(imageObserver);
			height = image.getHeight(imageObserver);

			if (width != -1 && height != -1) {
				pixels = new int[width * height];

				PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height,
						pixels, 0, width);

				pg.grabPixels();

				if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
					if (imageObserver.isErrored()) {
						throw new InterruptedException(
								"Error in grabbing Pixels");
					} else {
						throw new InterruptedException(
								"Grabbing Pixels aborted");
					}
				}
			}

			return pixels;
		}

		private int getWidth() {
			return width;
		}

		private int getHeight() {
			return height;
		}

		synchronized private void sizeKnown() {
			notify();
		}

		synchronized private int[] loadImage(String path)
				throws InterruptedException, FileNotFoundException {
			int[] pixels = null;

			File f = new File(path);

			if (f.exists()) {
				image = Toolkit.getDefaultToolkit().createImage(path);
				imageObserver = new ImageByteArrayObserver(this);
				Toolkit.getDefaultToolkit().prepareImage(image, -1, -1,
						imageObserver);
				wait();
				pixels = grabPixels();
			} else {
				String wd = "";
				if (path.charAt(0) != System.getProperty("file.separator")
						.charAt(0)) {
					wd = System.getProperty("user.dir")
							+ System.getProperty("file.separator").charAt(0);
				}
				throw new FileNotFoundException("File " + wd + path
						+ " not found");
			}

			return pixels;
		}
	}

	/**
	 * This method reads a image and provides its RGB color-components in an
	 * three-dimensional array to the caller.
	 * 
	 * @param fileName
	 *            the path to the image-source used for grabbing
	 * @return a three-dimensional integer array of the RGB values of the
	 *         pixels. The first index is the y-coordinate, the second index is
	 *         the x-coordinate into the image, and the third index is the
	 *         color-component. red = 0; green = 1; blue = 2.
	 * @throws FileNotFoundException
	 *             if the image specified in the path-argument does not exist.
	 * @throws InterruptedException
	 *             if the grabb-process is interrupted unexpectedly.
	 */
	public static int[][][] grabRGB(String fileName) throws FileNotFoundException,
			InterruptedException {
		ImageGrabberUtilities igu = new ImageGrabberUtilities();

		int[] pixels = igu.loadImage(fileName);

		int w = igu.getWidth();
		int h = igu.getHeight();

		int[][][] rgb = new int[h][w][];

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				rgb[y][x] = ImageGrabberUtilities
						.convertToRGB(pixels[y * w + x]);
			}
		}

		return rgb;
	}

	/**
	 * This method reads a image, converts it into gray-scale and provides this
	 * information in a two-dimensional array to the caller.
	 * 
	 * @param fileName
	 *            the path to the image-source used for grabbing
	 * @return a two-dimensional integer array of the gray-scale values of the
	 *         pixels. The first index is the y-coordinate and the second index
	 *         is the x-coordinate into the image
	 * @throws FileNotFoundException
	 *             if the image specified in the path-argument does not exist.
	 * @throws InterruptedException
	 *             if the grabb-process is interrupted unexpectedly.
	 */
	public static int[][] grabGrayScale(String fileName)
			throws FileNotFoundException, InterruptedException {
		ImageGrabberUtilities igu = new ImageGrabberUtilities();

		int[] pixels = igu.loadImage(fileName);

		int w = igu.getWidth();
		int h = igu.getHeight();

		int[][] grayScale = new int[h][w];

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				grayScale[y][x] = (int) (256 * ImageGrabberUtilities
						.convertToYCbCr(pixels[y * w + x])[0]);
			}
		}

		return grayScale;
	}
}
