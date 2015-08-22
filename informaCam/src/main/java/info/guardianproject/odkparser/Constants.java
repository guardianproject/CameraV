package info.guardianproject.odkparser;

public interface Constants {
	public final static class Logger {
		public final static String UI = "************ FORM UI *************";
		public final static String FORM = "************ FORM FORM HOLDER *************";
		public final static String PARSER = "************ FORM PARSER *************";
		public static final String WIDGET_FACTORY = "************ WIDGET FACTORY *************";
	}
	
	public final static class RecorderState {
		public final static int IS_IDLE = 1;
		public final static int IS_RECORDING = 2;
		public final static int IS_PLAYING = 3;
	}

	public final static class Form {
		public final static String[] SERIALIABLE_CLASSES = {
			"org.javarosa.core.model.FormDef", "org.javarosa.core.model.GroupDef",
			"org.javarosa.core.model.QuestionDef", "org.javarosa.core.model.data.DateData",
			"org.javarosa.core.model.data.DateTimeData",
			"org.javarosa.core.model.data.DecimalData",
			"org.javarosa.core.model.data.GeoPointData",
			"org.javarosa.core.model.data.helper.BasicDataPointer",
			"org.javarosa.core.model.data.IntegerData",
			"org.javarosa.core.model.data.MultiPointerAnswerData",
			"org.javarosa.core.model.data.PointerAnswerData",
			"org.javarosa.core.model.data.SelectMultiData",
			"org.javarosa.core.model.data.SelectOneData",
			"org.javarosa.core.model.data.StringData", "org.javarosa.core.model.data.TimeData",
			"org.javarosa.core.services.locale.TableLocaleSource",
			"org.javarosa.xpath.expr.XPathArithExpr", "org.javarosa.xpath.expr.XPathBoolExpr",
			"org.javarosa.xpath.expr.XPathCmpExpr", "org.javarosa.xpath.expr.XPathEqExpr",
			"org.javarosa.xpath.expr.XPathFilterExpr", "org.javarosa.xpath.expr.XPathFuncExpr",
			"org.javarosa.xpath.expr.XPathNumericLiteral",
			"org.javarosa.xpath.expr.XPathNumNegExpr", "org.javarosa.xpath.expr.XPathPathExpr",
			"org.javarosa.xpath.expr.XPathStringLiteral", "org.javarosa.xpath.expr.XPathUnionExpr",
			"org.javarosa.xpath.expr.XPathVariableReference"
		};
		
		public static final int MAX_QUESTIONS_PER_PAGE = 3;
		
		public static final class Keys {
			public static final String BIND_ID = "bindId";
		}
		
		public final static class Extras {
			public final static String DEFAULT_THUMB = "defaultThumb";
			public final static String DEFAULT_TITLE = "defaultTitle";
			public static final String PREVIOUS_ANSWERS = "previousAnswers";
			public static final String JSON_FORM = "jsonFormResults";
			public static final String EXPORT_MODE = "exportMode";
			public final static String THUMBNAIL = "regionThumbnail";
			public final static String DEF_PATH = "formDefPath";
			public static final String MAX_QUESTIONS_PER_PAGE = "maxQuestionsPerPage";
			public static final String DATA_DUMP = "dataDump";
		}
		
		public final static class ExportMode {
			public final static int JSON = 1;
			public final static int XML_URI = 2;
			public static final int XML_BAOS = 3;
		}
	}
}
