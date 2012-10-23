package org.xmlcml.graphics.pdf2svg.raw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.PDLineDashPattern;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorState;
import org.apache.pdfbox.pdmodel.text.PDTextState;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.TextPosition;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.graphics.svg.GraphicsElement.FontStyle;
import org.xmlcml.graphics.svg.GraphicsElement.FontWeight;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;

/** converts a PDPage to SVG
 * Originally used PageDrawer to capture the PDF operations.These have been
 * largely intercepted and maybe PageDrawer could be retired at some stage
 * @author pm286 and Murray Jensen
 *
 */
public class PDFPage2SVGConverter extends PageDrawer {

	
	private static final String BADCHAR_E = ">>";
	private static final String BADCHAR_S = "<<";
	private static final String FONT_NAME = "fontName";
	private static final String BOLD = "bold";
	private static final String ITALIC = "italic";
	private static final String OBLIQUE = "oblique";

	private final static Logger LOG = Logger.getLogger(PDF2SVGConverter.class);

	private static final Dimension DEFAULT_DIMENSION = new Dimension(800, 800);;
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private static double eps = 0.001;

	private BasicStroke basicStroke;
	private SVGSVG svg;
	private Composite composite;
	private Paint paint;
	private PDGraphicsState graphicsState;
	private Matrix textPos;
	private PDFont font;

	private Composite currentComposite;
	private String currentFontFamily;
	private String currentFontName;
	private Double currentFontSize;
	private String currentFontStyle;
	private String currentFontWeight;
	private Paint currentPaint;
	private String currentStroke;
	private SVGText currentSvgText;
	
	private int nPlaces = 3;
//	private String renderIntent;
	private PDLineDashPattern dashPattern;
	private Double lineWidth;
	private PDTextState textState;

	public PDFPage2SVGConverter() throws IOException {
		super();
	}

	public void drawPage(PDPage p) {
		ensurePageSize();
		page = p;

		try {
			if (page.getContents() != null) {
				PDResources resources = page.findResources();
				processStream(page, resources, page.getContents().getStream());
			}
		} catch (Exception e) {
			// PDFBox routines have a very bad feature of trapping exceptions
			// this is the best we can do to alert you at this stage
			e.printStackTrace();
			LOG.error("***FAILED " + e);
			throw new RuntimeException("drawPage", e);
		}

	}

	/** adds a default pagesize if not given
	 * 
	 */
	private void ensurePageSize() {
		if (pageSize == null) {
			pageSize = DEFAULT_DIMENSION;
		}
	}
	
//	protected void processTextPositionOld(TextPosition text) {
//
//		font = text.getFont();
//		List<Float> widthList = font.getWidths();
//		try {
//			float fw = font.getAverageFontWidth();
//			LOG.trace("average fontWidth: "+fw);
//		} catch (IOException e1) {
//			throw new RuntimeException("PDF exception", e1);
//		}
//		if (text.getCharacter().length() > 1) {
//			throw new RuntimeException("multi-char string"+text.getCharacter());
//		}
//		int charCode = text.getCharacter().charAt(0);
//		if (charCode > 255) {
//			LOG.warn("high codepoint "+charCode);
//		}
//		LOG.debug("Character width: "+((char)charCode)+" "+font.getFontWidth(charCode));
//		try {
//			LOG.debug("String width: "+((char)charCode)+" "+font.getStringWidth(text.getCharacter()));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		LOG.trace("W "+widthList.size());
//		LOG.trace("W "+Arrays.toString(widthList.toArray(new Float[0])));
//		// width example
//		//    :250  !:333  ":408  #:500  $:500  %:833  &:778  ':180  (:333  ):333  *:500  +:564  ,:250  -:333  .:250  /:278  0:500  1:500  2:500  3:500  4:500  5:500  6:500  7:500  8:500  9:500  ::278  ;:278  <:564  =:564  >:564  ?:444  @:921  A:722  B:667  C:667  D:722  E:611  F:556  G:722  H:722  I:333  J:389  K:722  L:611  M:889  N:722  O:722  P:556  Q:722  R:667  S:556  T:611  U:722  V:722  W:944  X:722  Y:722  Z:611  [:333  \:278  ]:333  ^:469  _:500  `:333  a:444  b:500  c:444  d:500  e:444  f:333  g:500  h:500  i:278  j:278  k:500  l:278  m:778  n:500  o:500  p:500  q:500  r:333  s:389  t:278  u:500  v:500  w:722  x:500  y:500  z:444  {:480  |:200  }:480  ~:541
//
////		for (int i = 32; i < 127; i++	)  {
////			System.out.print("  "+(char)i+":"+(int) (double) widthList.get(i));
//		
////		}
////		System.out.println();
//			
//		ensurePageSize();
//		try {
//			createNewG = false;
//			currentSvgText = new SVGText();
//			// clear current values
//			currentSvgText.setFontSize(null);
//			currentSvgText.setStroke(null);
//			currentSvgText.setFontWeight((FontWeight)null);
//			currentSvgText.setFontStyle((FontStyle)null);
//			normalizeFontFamilyNameStyleWeight();
//			String textContent = text.getCharacter();
//			try {
//				currentSvgText.setText(textContent);
//			} catch (RuntimeException e) {
//				// drops here if cannot encode as XML character
//				tryToConvertStrangeCharactersOrFonts(text, currentSvgText);
//			}
//			createGraphicsStateAndPaintAndComposite();
//			createAndReOrientateTextPosition(text, currentSvgText);
//			
//			createNewGIfCurrentFontDescriptorChanged();
//			createNewGIfFontSizeChanged(text, currentSvgText);
//			createNewGIfClipPathChanged();
//			checkStrokeUnchanged(currentSvgText);
//
//			currentSvgText.format(nPlaces);
//			if (createNewG) {
//				createNewGAndFillWithCurrentValues();
//			}
//			g.appendChild(currentSvgText);
//		} catch (Exception e) {
//			throw new RuntimeException("drawPage", e);
//		}
//	}

	@Override
	protected void processTextPosition(TextPosition text) {

		font = text.getFont();
		// for info
		String textContent = text.getCharacter();
		if (textContent.length() > 1) {
			// don't know when or whether this will happen. I hope that the length is always 1
			throw new RuntimeException("multi-char string"+text.getCharacter());
		}
		int charCode = text.getCharacter().charAt(0);
		if (charCode > 255) {
			LOG.warn("high codepoint "+charCode);
		}
		float width = getCharacterWidth(font, textContent);
		
		ensurePageSize();
		SVGText svgText = new SVGText();
		normalizeFontFamilyNameStyleWeight();
		if (currentFontWeight != null) {
			svgText.setFontWeight(currentFontWeight);
		}
		try {
			svgText.setText(textContent);
		} catch (RuntimeException e) {
			// drops here if cannot encode as XML character
			tryToConvertStrangeCharactersOrFonts(text, svgText);
		}
		createGraphicsStateAndPaintAndComposite();
		createAndReOrientateTextPosition(text, svgText);
		
		getFontSizeAndSetNotZeroRotations(svgText);
		svgText.setFontSize(currentFontSize);
		String stroke = getCSSColor((Color) paint);
		svgText.setStroke(stroke);
		if (currentFontStyle != null) {
			svgText.setFontStyle(currentFontStyle);
		}
		if (currentFontFamily != null) {
			svgText.setFontFamily(currentFontFamily);
		}
		if (currentFontName != null) {
			setFontName(svgText, currentFontName);
		}
		setCharacterWidth(svgText, width);

		svgText.format(nPlaces);
		svg.appendChild(svgText);
	}

	private float getCharacterWidth(PDFont font, String textContent) {
		float width = 0.0f;
		try {
			width = font.getStringWidth(textContent);
		} catch (IOException e) {
			throw new RuntimeException("PDFBox exception ", e);
		}
		return width;
	}

	private void tryToConvertStrangeCharactersOrFonts(TextPosition text, SVGText svgText) {
		char cc = text.getCharacter().charAt(0);
		String s = BADCHAR_S+(int)cc+BADCHAR_E;
		LOG.debug(s+" "+currentFontName);
		svgText.setText(s);
	}

	private String interpretCharacter(String fontFamily, char cc) {
		String s = null;
		if ("MathematicalPi-One".equals(fontFamily)) {
			if (cc == 1) {
				s = CMLConstants.S_PLUS;
			}
		} else {
			System.out.println("font "+fontFamily+" point "+(int)cc);
		}
		return s;
	}

	private void normalizeFontFamilyNameStyleWeight() {
		PDFontDescriptor fd = font.getFontDescriptor();
		currentFontFamily = fd.getFontFamily();
		currentFontName = fd.getFontName();
		// currentFontFamily may be null?
		if (currentFontName == null) {
			throw new RuntimeException("No currentFontName");
		}
		if (currentFontFamily == null) {
			currentFontFamily = currentFontName;
		}
		// strip leading characters (e.g. KAIKCD+Helvetica-Oblique);
		stripFamilyPrefix();
		createFontStyle();
		createFontWeight();
	}

	private void stripFamilyPrefix() {
		int index = currentFontFamily.indexOf("+");
		if (index != -1) {
			currentFontFamily = currentFontFamily.substring(index+1);
		}
	}
	
	private void createFontStyle() {
		String ff = currentFontFamily.toLowerCase();
		int idx = currentFontFamily.indexOf("-");
		String suffix = ff.substring(idx+1).toLowerCase();
		if (suffix.equals(OBLIQUE) || suffix.equals(ITALIC)) {
			currentFontStyle = FontStyle.ITALIC.toString().toLowerCase();
			currentFontFamily = currentFontFamily.substring(0, idx);
		} else {
			currentFontStyle = null;
		}
	}

	/** looks for -bold in font name
	 * 
	 */
	private void createFontWeight() {
		String ff = currentFontFamily.toLowerCase();
		int idx = ff.indexOf("-");
		String suffix = ff.substring(idx+1);
		if (suffix.equals(BOLD)) {
			currentFontWeight = FontWeight.BOLD.toString().toLowerCase();
			currentFontFamily = currentFontFamily.substring(0, idx);
		} else {
			currentFontWeight = null;
		}
	}

	/** translates java color to CSS RGB
	 * 
	 * @param paint
	 * @return CCC as #rrggbb (alpha is currently discarded)
	 */
	private static String getCSSColor(Paint paint) {
		int r = ((Color) paint).getRed();
		int g = ((Color) paint).getGreen();
		int b = ((Color) paint).getBlue();
		int a = ((Color) paint).getAlpha();
		int rgb = (r<<16)+(g<<8)+b;
		String colorS = String.format("#%06x", rgb);
		if (rgb != 0) {
			LOG.trace("Paint "+rgb+" "+colorS);
		}
		return colorS;
	}

//	private void createNewGIfFontSizeChanged(TextPosition text, SVGText svgText) {
//		AffineTransform at = textPos.createAffineTransform();
//		PDMatrix fontMatrix = font.getFontMatrix();
//		at.scale(fontMatrix.getValue(0, 0) * 1000f,
//				fontMatrix.getValue(1, 1) * 1000f);
//		double scalex = at.getScaleX();
//		double scaley = at.getScaleY();
//		double scale = Math.sqrt(scalex * scaley);
//		Transform2 t2 = new Transform2(at);
//		Angle angle = t2.getAngleOfRotation();
//		int angleDeg = Math.round((float)angle.getDegrees());
//		if (angleDeg != 0) {
//			LOG.trace("Transform "+t2+" "+svgText.getText()+" "+at+" "+getRealArray(fontMatrix));
//			// do this properly later
//			scale = Math.sqrt(Math.abs(t2.elementAt(0, 1)*t2.elementAt(1, 0)));
//			Transform2 t2a = Transform2.getRotationAboutPoint(angle, svgText.getXY());
//			svgText.setTransform(t2a);
//		}
//		if (hasChanged(scale, currentFontSize, eps)) {
//			createNewG = true;
//		}
//		currentFontSize = scale;
//	}

	private double getFontSizeAndSetNotZeroRotations(SVGText svgText) {
		AffineTransform at = textPos.createAffineTransform();
		PDMatrix fontMatrix = font.getFontMatrix();
		at.scale(fontMatrix.getValue(0, 0) * 1000f,
				fontMatrix.getValue(1, 1) * 1000f);
		double scalex = at.getScaleX();
		double scaley = at.getScaleY();
		double scale = Math.sqrt(scalex * scaley);
		Transform2 t2 = new Transform2(at);
		
		Angle angle = t2.getAngleOfRotation();
		int angleDeg = Math.round((float)angle.getDegrees());
		if (angleDeg != 0) {
			LOG.trace("Transform "+t2+" "+svgText.getText()+" "+at+" "+getRealArray(fontMatrix));
			// do this properly later
			scale = Math.sqrt(Math.abs(t2.elementAt(0, 1)*t2.elementAt(1, 0)));
			Transform2 t2a = Transform2.getRotationAboutPoint(angle, svgText.getXY());
			svgText.setTransform(t2a);
		}
		currentFontSize = scale;
		return currentFontSize;
	}

	private RealArray getRealArray(PDMatrix fontMatrix) {
		double[] dd = new double[9];
		int kk = 0;
		int nrow = 2;
		int ncol = 3;
		for (int irow = 0; irow < nrow; irow++) {
			for (int jcol = 0; jcol < ncol; jcol++) {
				dd[kk++] = fontMatrix.getValue(irow, jcol);
			}
		}
		RealArray ra = new RealArray(dd);
		return ra;
	}

	/** changes coordinates because AWT and SVG use top-left origin while PDF uses bottom left
	 * 
	 * @param text
	 * @param svgText
	 */
	private void createAndReOrientateTextPosition(TextPosition text, SVGText svgText) {
		textPos = text.getTextPos().copy();
		float x = textPos.getXPosition();
		// the 0,0-reference has to be moved from the lower left (PDF) to
		// the upper left (AWT-graphics)
		float y = pageSize.height - textPos.getYPosition();
		// Set translation to 0,0. We only need the scaling and shearing
		textPos.setValue(2, 0, 0);
		textPos.setValue(2, 1, 0);
		// because of the moved 0,0-reference, we have to shear in the
		// opposite direction
		textPos.setValue(0, 1, (-1) * textPos.getValue(0, 1));
		textPos.setValue(1, 0, (-1) * textPos.getValue(1, 0));
		svgText.setXY(new Real2(x, y));
	}

	private void createGraphicsStateAndPaintAndComposite() {
		try {
			graphicsState = getGraphicsState();
			switch (graphicsState.getTextState().getRenderingMode()) {
			case PDTextState.RENDERING_MODE_FILL_TEXT:
				composite = graphicsState.getNonStrokeJavaComposite();
				paint = graphicsState.getNonStrokingColor().getJavaColor();
				if (paint == null) {
					paint = graphicsState.getNonStrokingColor().getPaint(
							pageSize.height);
				}
				break;
			case PDTextState.RENDERING_MODE_STROKE_TEXT:
				composite = graphicsState.getStrokeJavaComposite();
				paint = graphicsState.getStrokingColor().getJavaColor();
				if (paint == null) {
					paint = graphicsState.getStrokingColor().getPaint(
							pageSize.height);
				}
				break;
			case PDTextState.RENDERING_MODE_NEITHER_FILL_NOR_STROKE_TEXT:
				// basic support for text rendering mode "invisible"
				Color nsc = graphicsState.getStrokingColor().getJavaColor();
				float[] components = { Color.black.getRed(),
						Color.black.getGreen(), Color.black.getBlue() };
				paint = new Color(nsc.getColorSpace(), components, 0f);
				composite = graphicsState.getStrokeJavaComposite();
				break;
			default:
				// TODO : need to implement....
				System.out.println("Unsupported RenderingMode "
						+ this.getGraphicsState().getTextState()
								.getRenderingMode()
						+ " in PageDrawer.processTextPosition()."
						+ " Using RenderingMode "
						+ PDTextState.RENDERING_MODE_FILL_TEXT + " instead");
				composite = graphicsState.getNonStrokeJavaComposite();
				paint = graphicsState.getNonStrokingColor().getJavaColor();
			}
		} catch (IOException e) {
			throw new RuntimeException("graphics state error???", e);
		}
	}

	/** traps any remaining unimplemented PDDrawer calls
	 * 
	 */
	public Graphics2D getGraphics() {
		System.err.printf("getGraphics was called!!!!!!! (May mean method was not overridden) %n");
		return null;
	}

	public void fillPath(int windingRule) throws IOException {
		PDColorState colorState = getGraphicsState().getNonStrokingColor();
		Paint currentPaint = getCurrentPaint(colorState, "non-stroking");
		createAndAddSVGPath(windingRule, currentPaint);
	}

	public void strokePath() throws IOException {
		PDColorState colorState = getGraphicsState().getStrokingColor(); 
		Paint currentPaint = getCurrentPaint(colorState, "stroking");
		Integer windingRule = null;
		createAndAddSVGPath(windingRule, currentPaint);
	}

	/** processes both stroke and fill for paths
	 * 
	 * @param windingRule if not null implies fill else stroke
	 * @param currentPaint
	 */
	private void createAndAddSVGPath(Integer windingRule, Paint currentPaint) {
//		renderIntent = getGraphicsState().getRenderingIntent(); // probably ignorable at first (converts color maps)
		dashPattern = getGraphicsState().getLineDashPattern();
		lineWidth = getGraphicsState().getLineWidth();
		textState = getGraphicsState().getTextState();  // has things like character and word spacings // not yet used
		GeneralPath generalPath = getLinePath();
		if (windingRule != null) {
			generalPath.setWindingRule(windingRule);
		}
		SVGPath svgPath = new SVGPath(generalPath);
		if (windingRule != null) {
			svgPath.setFill(getCSSColor(currentPaint));
		} else {
			svgPath.setStroke(getCSSColor(currentPaint));
		}
		if (dashPattern != null) {
			setDashArray(svgPath);
		}
		if (lineWidth > 0.00001) {
			svgPath.setStrokeWidth(lineWidth);
			LOG.trace("stroke "+lineWidth);
		}
		svgPath.format(nPlaces);
		svg.appendChild(svgPath);
		generalPath.reset();
	}

	private void setDashArray(SVGPath svgPath) {
		List<Integer> dashIntegerList = (List<Integer>) dashPattern.getDashPattern();
		StringBuilder sb = new StringBuilder("");
		LOG.trace("COS ARRAY "+dashIntegerList.size());
		if (dashIntegerList.size() > 0) {
			for (int i = 0; i < dashIntegerList.size(); i++) {
				if (i > 0) {
					sb.append(" ");
				}
				sb.append(dashIntegerList.get(i));
			}
			svgPath.setStrokeDashArray(sb.toString());
			LOG.debug("dash "+dashPattern);
		}
	}

	private Paint getCurrentPaint(PDColorState colorState, String type) throws IOException {
		Paint currentPaint = colorState.getJavaColor();
		if (currentPaint == null) {
			currentPaint = colorState.getPaint(pageSize.height);
		}
		if (currentPaint == null) {
			LOG.warn("ColorSpace "
					+ colorState.getColorSpace().getName()
					+ " doesn't provide a " + type
					+ " color, using white instead!");
			currentPaint = Color.WHITE;
		}
		return currentPaint;
	}

	/** maye be removed later
	 * 
	 */
	public void drawImage(Image awtImage, AffineTransform at) {
		System.out
				.printf("\tdrawImage: awtImage='%s', affineTransform='%s', composite='%s', clip='%s'%n",
						awtImage.toString(), at.toString(), getGraphicsState()
								.getStrokeJavaComposite().toString(),
						getGraphicsState().getCurrentClippingPath().toString());
		// LOG.error("drawImage Not yet implemented");
	}

	/** used in pageDrawer - shaded type of fill
	 * 
	 */
	public void shFill(COSName shadingName) throws IOException {
		throw new IOException("Not Implemented");
	}

	/** creates new <svg> and removes/sets some defaults
	 * this is partly beacuse SVGFoo may have defaults (bad idea?)
	 */
	public void resetSVG() {
		this.svg = new SVGSVG();
		svg.setStroke("none");
		svg.setStrokeWidth(0.0);
		svg.addNamespaceDeclaration(PDF2SVGUtil.SVGX_PREFIX, PDF2SVGUtil.SVGX_NS);
	}

	public SVGSVG getSVG() {
		return svg;
	}

	void convertPageToSVG(PDPage page) {
		resetSVG();
		drawPage(page);
	}
	
	private void setFontName(SVGElement svgElement, String fontName) {
		PDF2SVGUtil.setSVGXAttribute(svgElement, FONT_NAME, fontName);
	}
	
	private void setCharacterWidth(SVGElement svgElement, double width) {
		PDF2SVGUtil.setSVGXAttribute(svgElement, PDF2SVGUtil.CHARACTER_WIDTH, ""+width);
	}
	
	@Override
	public void setStroke(BasicStroke basicStroke) {
		this.basicStroke = basicStroke;
	}

	@Override
	public BasicStroke getStroke() {
		return basicStroke;
	}

	private static String fmtFont(PDFont font) {
		PDFontDescriptor fd = font.getFontDescriptor();
		String format = null;
		try {
			format = String
					.format("[family=%s,name:%s,weight=%f,angle=%f,charset=%s,avg-width=%f]",
							fd.getFontFamily(), fd.getFontName(),
							fd.getFontWeight(), fd.getItalicAngle(),
							fd.getCharSet(), fd.getAverageWidth());
		} catch (IOException e) {
			throw new RuntimeException("Average width problem", e);
		}
		return format;
	}


}