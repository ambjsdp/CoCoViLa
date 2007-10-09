package ee.ioc.cs.vsle.editor;

import java.awt.*;
import java.io.*;
import java.util.*;

import ee.ioc.cs.vsle.util.*;

public class RuntimeProperties {

    public static final String FS = System.getProperty( "file.separator" );
    public static final String PS = System.getProperty( "path.separator" );

    public final static String SCHEME_DTD = "scheme.dtd";
    public final static String PACKAGE_DTD = "package2.dtd";
    public final static String PACKAGE_LOCATOR = "package.locator";

    public static final String APP_PROPS_FILE_NAME = "config.xml";
    public static final String GPL_EN_SHORT_LICENSE_FILE_NAME = "resources/gpl_en_short.txt";
    public static final String GPL_EN_LICENSE_FILE_NAME = "resources/gpl_en.txt";
    public static final String GPL_EE_LICENSE_FILE_NAME = "resources/gpl_ee.txt";

    /**
     * Property names (keys) found in the configuration file.
     */
    private static final String DOCUMENTATION_URL = "documentation.url";
    private static final String GENERATED_FILES_DIR = "generatedFilesDirectory";
    private static final String OPEN_PACKAGES = "openPackages";
    private static final String DEBUG_INFO = "debugInfo";
    private static final String DEFAULT_LNF = "defaultLayout";
    private static final String LAST_PATH = "last.path";
    private static final String LAST_EXECUTED = "lastExecuted";
    private static final String ANTI_ALIASING = "antiAliasing";
    private static final String SHOW_GRID = "showGrid";
    private static final String GRID_STEP = "gridStep";
    private static final String NUDGE_STEP = "nudgeStep";
    private static final String SNAP_TO_GRID = "snapToGrid";
    private static final String RECENT_PACKAGES = "recentPackages";
    private static final String COMPILATION_CLASSPATH = "compilationClasspath";
    private static final String ZOOM_LEVEL = "defaultzoom";
    private static final String SYNTAX_HIGHLIGHT = "syntax_highlight";
    private static final String SHOW_ALGORITHM = "show_algorithm";
    private static final String TEXT_FONT = "text_font";
    private static final String VERSION = "version";
    private static final String VERSION_UNKNOWN = "@project.version@";
    //x;y;width;height;state
    private static final String SCHEME_EDITOR_WINDOW_PROPS = "schemeEditorWindowProps";

    private static boolean fromWebstart = false;
    private static String workingDirectory = System.getProperty( "user.dir" ) + FS;
    
    private static final Properties s_defaultProperties;
    private static final Properties s_runtimeProperties;

    static {

        s_defaultProperties = new Properties();
        s_runtimeProperties = new Properties( s_defaultProperties );

        s_defaultProperties.put( DOCUMENTATION_URL, "http://www.cs.ioc.ee/~cocovila/" );
        s_defaultProperties.put( GENERATED_FILES_DIR, isFromWebstart() ? getWorkingDirectory() + "generated" : "generated" );
        s_defaultProperties.put( COMPILATION_CLASSPATH, isFromWebstart() ? "" : "lib/jcommon.jar;lib/jfreechart.jar" );
        s_defaultProperties.put( OPEN_PACKAGES, "" );
        s_defaultProperties.put( DEBUG_INFO, Integer.toString( 0 ) );
        s_defaultProperties.put( DEFAULT_LNF, "javax.swing.plaf.metal.MetalLookAndFeel" );
        s_defaultProperties.put( ANTI_ALIASING, Boolean.TRUE.toString() );
        s_defaultProperties.put( SHOW_GRID, Boolean.TRUE.toString() );
        s_defaultProperties.put( GRID_STEP, Integer.toString( 15 ) );
        s_defaultProperties.put( NUDGE_STEP, Integer.toString( 1 ) );
        s_defaultProperties.put( SNAP_TO_GRID, Boolean.FALSE.toString() );
        s_defaultProperties.put( RECENT_PACKAGES, "" );
        s_defaultProperties.put( ZOOM_LEVEL, Float.toString( 1.0f ) );
        s_defaultProperties.put( SYNTAX_HIGHLIGHT, Boolean.TRUE.toString() );
        s_defaultProperties.put( SHOW_ALGORITHM, Boolean.FALSE.toString() );
        s_defaultProperties.put( TEXT_FONT, "Courier New-plain-12" );
        s_defaultProperties.put( VERSION, VERSION_UNKNOWN );
        s_defaultProperties.put( SCHEME_EDITOR_WINDOW_PROPS, ";;650;600;0" );
    }

    private static String genFileDir;
    private static String compilationClasspath;
    private static int debugInfo;
    private static int gridStep;
    private static int nudgeStep;
    private static boolean snapToGrid;
    private static boolean showGrid;
    private static boolean isAntialiasingOn;
    private static float zoomFactor;
    private static boolean isSyntaxHighlightingOn;
    private static boolean showAlgorithm;
    private static String lnf;
    private static Font font;
    private static Set<String> openPackages = new LinkedHashSet<String>();
    private static Map<String, String> recentPackages = new LinkedHashMap<String, String>();
    
    public static boolean isLogDebugEnabled() {
        return getDebugInfo() >= 1;
    }

    public static boolean isLogInfoEnabled() {
        return getDebugInfo() >= 0;
    }

    public static boolean isFromWebstart() {
        return fromWebstart;
    }

    public static void setFromWebstart() {

        RuntimeProperties.fromWebstart = true;

        workingDirectory = System.getProperty( "user.home" ) + FS + "CoCoViLa" + FS;

        File file = new File( workingDirectory );

        file.mkdirs();

        System.setProperty( "user.dir", workingDirectory );
    }

    public static String getWorkingDirectory() {
        return workingDirectory;
    }

    public static void init() {

        readProperties( APP_PROPS_FILE_NAME, s_runtimeProperties );

        // Initialize runtime environment before loading any packages
        // that might need compilation of classpainters or something.
        setGenFileDir( s_runtimeProperties.getProperty( GENERATED_FILES_DIR ) );

        File file = new File( RuntimeProperties.getGenFileDir() );
        if ( !file.exists() ) {
            file.mkdirs();
        }

        setCompilationClasspath( s_runtimeProperties.getProperty( COMPILATION_CLASSPATH ) );

        setLnf( s_runtimeProperties.getProperty( DEFAULT_LNF ) );
        setDebugInfo( Integer.parseInt( s_runtimeProperties.getProperty( DEBUG_INFO ) ) );
        setGridStep( Integer.parseInt( s_runtimeProperties.getProperty( GRID_STEP ) ) );
        setAntialiasingOn( Boolean.parseBoolean( s_runtimeProperties.getProperty( ANTI_ALIASING ) ) );
        setSnapToGrid( Boolean.parseBoolean( s_runtimeProperties.getProperty( SNAP_TO_GRID ) ) );
        setShowGrid( Boolean.parseBoolean( s_runtimeProperties.getProperty( SHOW_GRID ) ) );
        setZoomFactor( Float.parseFloat( s_runtimeProperties.getProperty( ZOOM_LEVEL ) ) );
        setNudgeStep( Integer.parseInt( s_runtimeProperties.getProperty( NUDGE_STEP ) ) );
        setShowAlgorithm( Boolean.parseBoolean( s_runtimeProperties.getProperty( SHOW_ALGORITHM ) ) );
        setSyntaxHighlightingOn( Boolean.parseBoolean( s_runtimeProperties.getProperty( SYNTAX_HIGHLIGHT ) ) );
        setFont( Font.decode( s_runtimeProperties.getProperty( TEXT_FONT ) ) );
        String openPacks = s_runtimeProperties.getProperty( OPEN_PACKAGES );

        if ( openPacks != null && openPacks.trim().length() > 0 ) {
            String[] pack = openPacks.split( ";" );

            for ( int i = 0; i < pack.length; i++ ) {

                if ( pack[ i ].trim().length() == 0 )
                    continue;

                RuntimeProperties.openPackages.add( pack[ i ] );
            }
        }

        String recent = s_runtimeProperties.getProperty( RECENT_PACKAGES );

        if ( recent != null ) {

            String[] packages = recent.split( ";" );

            for ( int i = 0; i < packages.length; i++ ) {

                final File f = new File( packages[ i ] );

                if ( f.exists() ) {

                    String packageName = f.getName().substring( 0, f.getName().indexOf( "." ) );

                    RuntimeProperties.recentPackages.put( packageName, f.getAbsolutePath() );
                }
            }
        }

        s_runtimeProperties.setProperty( LAST_EXECUTED, new java.util.Date().toString() );

    }

    public static void save() {

        s_runtimeProperties.setProperty( GENERATED_FILES_DIR, genFileDir );
        s_runtimeProperties.setProperty( COMPILATION_CLASSPATH, compilationClasspath );
        s_runtimeProperties.setProperty( DEBUG_INFO, Integer.toString( debugInfo ) );
        s_runtimeProperties.setProperty( GRID_STEP, Integer.toString( gridStep ) );
        s_runtimeProperties.setProperty( NUDGE_STEP, Integer.toString( nudgeStep ) );
        s_runtimeProperties.setProperty( SNAP_TO_GRID, Boolean.toString( snapToGrid ) );
        s_runtimeProperties.setProperty( SHOW_GRID, Boolean.toString( showGrid ) );
        s_runtimeProperties.setProperty( ANTI_ALIASING, Boolean.toString( isAntialiasingOn ) );
        s_runtimeProperties.setProperty( ZOOM_LEVEL, Double.toString( zoomFactor ) );
        s_runtimeProperties.setProperty( SYNTAX_HIGHLIGHT, Boolean.toString( isSyntaxHighlightingOn ) );
        s_runtimeProperties.setProperty( SHOW_ALGORITHM, Boolean.toString( showAlgorithm ) );
        s_runtimeProperties.setProperty( DEFAULT_LNF, lnf );

        String fontString = font.getName() + "-";

        if ( font.isBold() ) {
            fontString += font.isItalic() ? "bolditalic" : "bold";
        } else {
            fontString += font.isItalic() ? "italic" : "plain";
        }

        fontString += "-" + font.getSize();

        s_runtimeProperties.setProperty( TEXT_FONT, fontString );

        String openPackagesString = "";

        for ( String open : openPackages ) {
            openPackagesString += open + ";";
        }

        s_runtimeProperties.setProperty( OPEN_PACKAGES, openPackagesString );

        String recentPackagesString = "";

        for ( String recent : recentPackages.values() ) {
            recentPackagesString += recent + ";";
        }

        s_runtimeProperties.setProperty( RECENT_PACKAGES, recentPackagesString );

        writeProperties( APP_PROPS_FILE_NAME, s_runtimeProperties );
        
        db.p( "Configuration saved" );
    }

    /**
     * Get system documentation URL value.
     * 
     * @return String - system documentation URL.
     */
    public static String getSystemDocUrl() {
        return s_runtimeProperties.getProperty( DOCUMENTATION_URL );
    }

    /**
     * Stores the last path used for loading or saving schema, package, etc.
     * into system properties.
     * 
     * @param path - last path used for loading or saving schema, package, etc.
     */
    public static void setLastPath( String path ) {
        if ( path != null ) {
            if ( path.indexOf( "/" ) > -1 ) {
                path = path.substring( 0, path.lastIndexOf( "/" ) );
            } else if ( path.indexOf( "\\" ) > -1 ) {
                path = path.substring( 0, path.lastIndexOf( "\\" ) );
            }
        }

        s_runtimeProperties.setProperty( LAST_PATH, path );
    }

    /**
     * Get last file path used for loading or saving schema, package, etc. from /
     * into a file.
     * 
     * @return String - last used path from system properties.
     */
    public static String getLastPath() {
        return s_runtimeProperties.getProperty( LAST_PATH );
    }

    /**
     * @param genFileDir the genFileDir to set
     */
    public static void setGenFileDir( String genFileDir ) {
        RuntimeProperties.genFileDir = genFileDir;
    }

    /**
     * @return the genFileDir
     */
    public static String getGenFileDir() {
        return genFileDir;
    }

    /**
     * @param compilationClasspath the compilationClasspath to set
     */
    public static void setCompilationClasspath( String compilationClasspath ) {

        if ( compilationClasspath == null ) {
            compilationClasspath = "";
        }
        RuntimeProperties.compilationClasspath = compilationClasspath;
    }

    /**
     * @return the compilationClasspath
     */
    public static String getCompilationClasspath() {
        return compilationClasspath;
    }

    /**
     * @param debugInfo the debugInfo to set
     */
    public static void setDebugInfo( int debugInfo ) {
        RuntimeProperties.debugInfo = debugInfo;
    }

    /**
     * @return the debugInfo
     */
    public static int getDebugInfo() {
        return debugInfo;
    }

    /**
     * @param gridStep the gridStep to set
     */
    public static void setGridStep( int gridStep ) {

        RuntimeProperties.gridStep = gridStep;
    }

    /**
     * @return the gridStep
     */
    public static int getGridStep() {
        return gridStep;
    }

    /**
     * @param nudgeStep the nudgeStep to set
     */
    public static void setNudgeStep( int nudgeStep ) {
        RuntimeProperties.nudgeStep = nudgeStep;
    }

    /**
     * @return the nudgeStep
     */
    public static int getNudgeStep() {
        return nudgeStep;
    }

    /**
     * @param snapToGrid the snapToGrid to set
     */
    public static void setSnapToGrid( boolean snapToGrid ) {
        RuntimeProperties.snapToGrid = snapToGrid;
    }

    /**
     * @return the snapToGrid
     */
    public static boolean getSnapToGrid() {
        return snapToGrid;
    }

    /**
     * @param showGrid the showGrid to set
     */
    public static void setShowGrid( boolean showGrid ) {
        RuntimeProperties.showGrid = showGrid;
    }

    /**
     * @return the showGrid
     */
    public static boolean isShowGrid() {
        return showGrid;
    }

    /**
     * @param isAntialiasingOn the isAntialiasingOn to set
     */
    public static void setAntialiasingOn( boolean isAntialiasingOn ) {
        RuntimeProperties.isAntialiasingOn = isAntialiasingOn;
    }

    /**
     * @return the isAntialiasingOn
     */
    public static boolean isAntialiasingOn() {
        return isAntialiasingOn;
    }

    /**
     * @param zoomFactor the zoomFactor to set
     */
    public static void setZoomFactor( float zoomFactor ) {
        RuntimeProperties.zoomFactor = zoomFactor;
    }

    /**
     * @return the zoomFactor
     */
    public static float getZoomFactor() {
        return zoomFactor;
    }

    /**
     * @param isSyntaxHighlightingOn the isSyntaxHighlightingOn to set
     */
    public static void setSyntaxHighlightingOn( boolean isSyntaxHighlightingOn ) {
        RuntimeProperties.isSyntaxHighlightingOn = isSyntaxHighlightingOn;
    }

    /**
     * @return the isSyntaxHighlightingOn
     */
    public static boolean isSyntaxHighlightingOn() {
        return isSyntaxHighlightingOn;
    }

    /**
     * @param showAlgorithm the showAlgorithm to set
     */
    public static void setShowAlgorithm( boolean showAlgorithm ) {
        RuntimeProperties.showAlgorithm = showAlgorithm;
    }

    /**
     * @return the showAlgorithm
     */
    public static boolean isShowAlgorithm() {
        return showAlgorithm;
    }

    /**
     * @param openPackages the openPackages to set
     */
    public static void addOpenPackage( String openPackage ) {
        RuntimeProperties.openPackages.add( openPackage );

        String packageName = openPackage.substring( 0, openPackage.indexOf( "." ) );
        packageName = packageName.substring( packageName.lastIndexOf( "\\" ) + 1 );
        RuntimeProperties.recentPackages.put( packageName, openPackage );
    }

    public static void removeOpenPackage( String package_ ) {
        RuntimeProperties.openPackages.remove( package_ );
    }

    /**
     * @return the openPackages
     */
    public static Set<String> getOpenPackages() {
        return openPackages;
    }

    /**
     * @return the recentPackages
     */
    public static Map<String, String> getRecentPackages() {
        return recentPackages;
    }

    /**
     * @param lnf the lnf to set
     */
    public static void setLnf( String lnf ) {
        RuntimeProperties.lnf = lnf;
    }

    /**
     * @return the lnf
     */
    public static String getLnf() {
        return lnf;
    }

    /**
     * @param font the font to set
     */
    public static void setFont( Font font ) {
        RuntimeProperties.font = font;
    }

    /**
     * @return the font
     */
    public static Font getFont() {
        return font;
    }

    /**
     * Returns the version string of the currently running version of the
     * application.
     * 
     * @return the version string, null if not known
     */
    public static String getApplicationVersion() {
        // The version information is added at build time by ANT so the
        // version number is missing if the application was not compiled
        // by ANT. We assume here that if the user is not running an
        // "official" release then the user knows about CVS and other means
        // to obtain that information. The version number read from the file
        // application.properties distributed with the application should
        // have the form x.y.z-dev in case it is a CVS snapshot release.
        // Real releases should have version numbers without the -dev suffix.

        return s_runtimeProperties.getProperty( VERSION );
    }

    public static void readProperties( String propFile, Properties props ) {

        String wd = RuntimeProperties.getWorkingDirectory();

        File file = new File( wd + propFile );

        if ( file.exists() ) {

            try {

                FileInputStream in = new FileInputStream( file );

                try {
                    props.loadFromXML( in );
                } finally {
                    in.close();
                }
            } catch ( Exception e ) {
                db.p( "Error reading configuration properties" );
                e.printStackTrace();
            }
        }
    }

    public static void writeProperties( String propFile, Properties props ) {

        String wd = RuntimeProperties.getWorkingDirectory();

        try {

            FileOutputStream out = new FileOutputStream( wd + propFile );

            try {
                props.storeToXML( out, null );
            } finally {
                out.close();
            }
        } catch ( Exception e ) {
            db.p( "Error writing configuration properties" );
            e.printStackTrace();
        }
    }

    public static String getSchemeEditorWindowProps() {
        return s_runtimeProperties.getProperty( SCHEME_EDITOR_WINDOW_PROPS );
    }

    public static void setSchemeEditorWindowProps( Rectangle bounds, int winState ) {
        s_runtimeProperties.setProperty( SCHEME_EDITOR_WINDOW_PROPS, 
                bounds.x + ";" + bounds.y + ";" + bounds.width + ";" + bounds.height + ";" + winState );
    }
}
