package ee.ioc.cs.vsle.packageparse;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import ee.ioc.cs.vsle.editor.*;
import ee.ioc.cs.vsle.graphics.*;
import ee.ioc.cs.vsle.graphics.Image;
import ee.ioc.cs.vsle.graphics.Polygon;
import ee.ioc.cs.vsle.synthesize.*;
import ee.ioc.cs.vsle.util.*;
import ee.ioc.cs.vsle.vclass.*;

@Deprecated
public class PackageParser implements DiagnosticsCollector.Diagnosable {

    private VPackage pack;
    private String path;
    private DiagnosticsCollector collector = new DiagnosticsCollector();

    public static final EntityResolver ENTITY_RESOLVER = new EntityResolver() {

        public InputSource resolveEntity( String publicId, String systemId ) {
            if ( systemId != null && systemId.endsWith( "dtd" ) ) {
                URL url = FileFuncs.getResource( RuntimeProperties.PACKAGE_DTD, true );
                if ( url != null ) {
                    return new InputSource( url.toString() );
                }
                //if unable to find dtd in local fs, try getting it from web
                return new InputSource( RuntimeProperties.SCHEMA_LOC + RuntimeProperties.PACKAGE_DTD );
            }
            return null;
        }

    };

    public boolean load( File file ) {
        long startParsing = System.currentTimeMillis();

        pack = new VPackage( file.getAbsolutePath() );

        if ( RuntimeProperties.isLogDebugEnabled() )
            db.p( "Parsing package: " + pack.getPath() );

        // Use an instance of ourselves as the SAX event handler
        DefaultHandler handler = new PackageHandler();

        // Use the validating parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        // factory.setFeature("http://xml.org/sax/features/validation", true);
        factory.setValidating( true );
        path = file.getParent() + File.separator;

        try {
            // Parse the input
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse( file, handler );
        } catch ( SAXException sxe ) {
            // Error generated by this application (or a parser-initialization
            // error)
            Exception x = sxe;

            if ( sxe.getException() != null ) {
                x = sxe.getException();
            }

            collector.collectDiagnostic( "Parsing error: " + x.getMessage() );
            return false;
        } catch ( ParserConfigurationException pce ) {
            // Parser with specified options can't be built
            collector.collectDiagnostic( "Parser Configuration error: " + pce.getMessage() );
            return false;
        } catch ( IOException ioe ) {
            // I/O error
            collector.collectDiagnostic( "IO error: " + ioe.getMessage() );
            return false;
        }
//        if ( RuntimeProperties.isLogDebugEnabled() )
            db.p( "Package parsing completed in " + ( System.currentTimeMillis() - startParsing ) + "ms.\n" );

        return true;
    }

    /**
     * Returns the list of diagnostic messages generated.
     * @return diagnostic messages
     */
    public DiagnosticsCollector getDiagnostics() {
        return collector;
    }

    // ===========================================================
    // SAX DocumentHandler methods
    // ===========================================================
    private class PackageHandler extends DefaultHandler {

        /*
         * Constants for package file format tokens
         */
        private static final String ATR_ARC_ANGLE = "arcAngle";
        private static final String ATR_START_ANGLE = "startAngle";
        private static final String EL_PAINTER = "painter";
        private static final String EL_ICON = "icon";
        private static final String ATR_Y2 = "y2";
        private static final String ATR_Y1 = "y1";
        private static final String ATR_X2 = "x2";
        private static final String ATR_X1 = "x1";
        private static final String ATR_LINETYPE = "linetype";
        private static final String ATR_FIXEDSIZE = "fixedsize";
        private static final String ATR_FONTSIZE = "fontsize";
        private static final String ATR_FONTSTYLE = "fontstyle";
        private static final String ATR_FONTNAME = "fontname";
        private static final String VAL_F = "f";
        private static final String VAL_RF = "rf";
        private static final String ATR_STRING = "string";
        private static final String EL_BOUNDS = "bounds";
        private static final String ATR_TRANSPARENCY = "transparency";
        private static final String ATR_STROKE = "stroke";
        private static final String ATR_FILLED = "filled";
        private static final String ATR_COLOUR = "colour";
        private static final String ATR_HEIGHT = "height";
        private static final String ATR_WIDTH = "width";
        private static final String EL_RECT = "rect";
        private static final String EL_LINE = "line";
        private static final String EL_TEXT = "text";
        private static final String EL_IMAGE = "image";
        private static final String ATR_PATH = "path";
        private static final String ATR_FIXED = "fixed";
        private static final String ATR_DESCRIPTION = "description";
        private static final String EL_DESCRIPTION = "description";
        private static final String ATR_VALUE = "value";
        private static final String EL_FIELD = "field";
        private static final String EL_POINT = "point";
        private static final String EL_POLYGON = "polygon";
        private static final String EL_FIELDS = "fields";
        private static final String EL_CLOSED = "closed";
        private static final String EL_KNOWN = "known";
        private static final String EL_DEFAULT = "default";
        private static final String EL_OPEN = "open";
        private static final String ATR_STRICT = "strict";
        private static final String ATR_MULTI = "multi";
        private static final String ATR_PORT_CONNECTION = "portConnection";
        private static final String ATR_Y = "y";
        private static final String VAL_TRUE = "true";
        private static final String ATR_X = "x";
        private static final String ATR_NAME = "name";
        private static final String EL_NAME = "name";
        private static final String ATR_ID = "id";
        private static final String EL_PORT = "port";
        private static final String ATR_SHOW_FIELDS = "showFields";
        private static final String EL_GRAPHICS = "graphics";
        private static final String VAL_RELATION = "relation";
        private static final String ATR_TYPE = "type";
        private static final String EL_CLASS = "class";
        private static final String EL_PACKAGE = "package";
        private static final String ATR_STATIC = "static";
        private static final String ATR_NATURE = "nature";
        private static final String ATR_HIDDEN = "hidden";
        
        private final int CLASS = 1, PORT_OPEN = 2, PORT_CLOSED = 3, PACKAGE = 4, FIELD = 5, FIELD_KNOWN = 6;

        /**
         * The default alpha value
         */
        private static final int ALPHA_IMPLIED = 255;

        private StringBuilder charBuf;
        private String element;
        private int status;
        private PackageClass newClass;
        private ClassGraphics newGraphics;
        private Port newPort;
        private ClassField newField;
        private ArrayList<String> polyXs = new ArrayList<String>();
        private ArrayList<String> polyYs = new ArrayList<String>();
        private Polygon polygon;

        // Default constructor with proper visibility
        PackageHandler() {
            super();
        }

        @Override
        public void startDocument() {
            if ( charBuf == null )
                charBuf = new StringBuilder();
        }

        @Override
        public InputSource resolveEntity( String publicId, String systemId ) throws IOException, SAXException {

            return ENTITY_RESOLVER.resolveEntity( publicId, systemId );
        }

        @Override
        public void error( SAXParseException spe ) {
            // Error generated by the parser

            db.p( "\n** Parsing error, line " + spe.getLineNumber() + ", uri " + spe.getSystemId() );

            // Use the contained exception, if any
            Exception x = spe;

            if ( spe.getException() != null ) {
                x = spe.getException();
            }

            db.p( "** " + x.getMessage() );
        }

        /**
         * Output warnings via the debug output module. SAX ErrorHandler method.
         * 
         * @param e SAXParseException
         */
        @Override
        public void warning( SAXParseException e ) {
            db.p( "** Warning, line " + e.getLineNumber() + ", uri " + e.getSystemId() );
            db.p( "   " + e.getMessage() );
        } // warning

        @Override
        public void setDocumentLocator( Locator l ) { // Save this to resolve
            // relative URIs or to give
            // diagnostics.
        }

        @Override
        public void startElement( String namespaceURI, String lName, String qName, Attributes attrs ) {

            element = qName;
            if ( element.equals( EL_PACKAGE ) ) {
                status = PACKAGE;
            } else if ( element.equals( EL_CLASS ) ) {
                newClass = new PackageClass();
                status = CLASS;
                String type = attrs.getValue( ATR_TYPE );//done
                if ( type != null ) {
                    newClass.setComponentType( PackageClass.ComponentType.getType( type ) );
                }

                newClass.setStatic( Boolean.parseBoolean( attrs.getValue( ATR_STATIC ) ) );//done

            } else if ( element.equals( EL_GRAPHICS ) ) {
                newGraphics = new ClassGraphics();
                String showFields = attrs.getValue( ATR_SHOW_FIELDS );//done
                if ( showFields != null && showFields.equals( VAL_TRUE ) ) {
                    newGraphics.setShowFields( true );
                }
                String type = attrs.getValue( ATR_TYPE );//TODO wtf is this?
                if ( type != null && type.equals( VAL_RELATION ) ) {
                    newGraphics.setRelation( true );
                }
            } else if ( element.equals( EL_PORT ) ) {//done
                status = PORT_OPEN;
                String id = attrs.getValue( ATR_ID );
                String name = attrs.getValue( ATR_NAME );
                String type = attrs.getValue( ATR_TYPE );
                String x = attrs.getValue( ATR_X );
                String y = attrs.getValue( ATR_Y );
                String portConnection = attrs.getValue( ATR_PORT_CONNECTION );
                String strict = attrs.getValue( ATR_STRICT );
                String multi = attrs.getValue( ATR_MULTI );

                ClassField cf = newClass.getSpecField( name );

                if(newClass.getComponentType() != PackageClass.ComponentType.SCHEME) {

                    if ( name.indexOf( "." ) > -1 ) {
                        //TODO - temporarily do not dig into hierarchy
                        int idx = name.indexOf( "." );
                        String root = name.substring( 0, idx );

                        if ( newClass.getSpecField( root ) == null ) {
                            collector.collectDiagnostic( "Field " + root + " in class " + newClass.getName()
                                    + " is not declared in the specification, variable " + type + " " + name + " ignored " );
                            return;
                        }

                        newField = new ClassField( name, type );
                        newClass.addSpecField( newField );
                    } else if ( !TypeUtil.TYPE_THIS.equalsIgnoreCase( name ) ) {
                        if ( cf == null ) {

                            collector.collectDiagnostic( "Port " + type + " " + name + " in class " + newClass.getName()
                                    + " does not have the corresponding field in the specification" );
                        } else if ( !cf.getType().equals( type )
                                //type may be declared as "alias", however cf.getType() returns e.g. "double[]", ignore it
                                && !( cf.isAlias() && TypeUtil.TYPE_ALIAS.equals( type )) ) {

                            collector.collectDiagnostic( "Port " + type + " " + name + " in class " + newClass.getName()
                                    + " does not match the field declared in the specification: " + cf.getType() + " " + cf.getName() );
                        }
                    }
                }
                newPort = new Port( name, type, Integer.parseInt( x ), Integer.parseInt( y ), portConnection, strict, multi );
                newPort.setId( id );
            } else if ( element.equals( EL_OPEN ) ) {
                status = PORT_OPEN;
            } else if ( element.equals( EL_DEFAULT ) ) {
                status = FIELD;
            } else if ( element.equals( EL_KNOWN ) ) {
                status = FIELD_KNOWN;
            } else if ( element.equals( EL_CLOSED ) ) {
                status = PORT_CLOSED;
            } else if ( element.equals( EL_FIELDS ) ) {
            } else if ( element.equals( EL_POLYGON ) ) {
                // initiate them to gather all X and Y positions there.
                makeInitialPolygon( attrs );

            } else if ( element.equals( EL_POINT ) ) {//done
                String x = attrs.getValue( ATR_X );
                String y = attrs.getValue( ATR_Y );
                polyXs.add( x );
                polyYs.add( y );
            } else if ( element.equals( EL_FIELD ) ) {//done
                String name = attrs.getValue( ATR_NAME );
                String type = attrs.getValue( ATR_TYPE );
                String value = attrs.getValue( ATR_VALUE );
                String desc = attrs.getValue( ATR_DESCRIPTION );

                if ( name.indexOf( "." ) > -1 ) {
                    //TODO - temporarily do not dig into hierarchy
                    int idx = name.indexOf( "." );
                    String root = name.substring( 0, idx );

                    if ( newClass.getSpecField( root ) == null ) {
                        collector.collectDiagnostic( "Field " + root + " in class " + newClass.getName()
                                + " is not declared in the specification, variable " + type + " " + name + " ignored " );
                        return;
                    }

                    newField = new ClassField( name, type );
                    newClass.addSpecField( newField );
                } else {
                    newField = newClass.getSpecField( name );

                    if ( newField == null ) {

                        collector.collectDiagnostic( "Field " + type + " " + name + " in class " + newClass.getName()
                                + " is not declared in the specification" );
                        return;
                    } else if ( !newField.getType().equals( type ) ) {

                        collector.collectDiagnostic( "Field " + type + " " + name + " in class " + newClass.getName()
                                + " does not match the field declared in the specification: " + newField.getType() + " "
                                + newField.getName() );
                        return;
                    }
                }

                newField.setValue( value );
                newField.setDescription( desc );
                newField.setHidden( Boolean.parseBoolean( attrs.getValue( ATR_HIDDEN ) ) );
                
                String nature = attrs.getValue( ATR_NATURE );
                if ( "input".equals( nature ) )
                    newField.setInput( true );
                else if ( "goal".equals( nature ) )
                    newField.setGoal( true );

                newClass.addField( newField );

            } else if ( element.equals( EL_TEXT ) ) {//done
                Text newText = makeText( attrs, newGraphics );

                /*
                 * if (str.equals("*self")) newText.name = "self"; else if
                 * (str.equals("*selfWithName")) newText.name = "selfName";
                 */
                newGraphics.addShape( newText );

            } else if ( element.equals( EL_LINE ) ) {//done
                Line newLine = makeLine( attrs, newGraphics );

                newGraphics.addShape( newLine );
            } else if ( element.equals( EL_IMAGE ) ) {//done
                String x = attrs.getValue( ATR_X );
                String y = attrs.getValue( ATR_Y );
//                String width = attrs.getValue( ATR_WIDTH );
//                String height = attrs.getValue( ATR_HEIGHT );
                String fixed = attrs.getValue( ATR_FIXED );
                String imgPath = attrs.getValue( ATR_PATH );
                
                String fullPath = FileFuncs.preparePathOS( PackageParser.this.path + imgPath );
                
                Image newImg = new Image( Integer.parseInt( x ), Integer.parseInt( y ), 
                        fullPath, imgPath,
                        ( fixed != null ) ? Boolean.parseBoolean( fixed ) : true );
                
                newGraphics.addShape( newImg );
                
            } else if ( element.equals( EL_RECT ) ) {//done
                String x = attrs.getValue( ATR_X );
                String y = attrs.getValue( ATR_Y );
                String width = attrs.getValue( ATR_WIDTH );
                String height = attrs.getValue( ATR_HEIGHT );
                String color = attrs.getValue( ATR_COLOUR );
                String filled = attrs.getValue( ATR_FILLED );
                String stroke = attrs.getValue( ATR_STROKE );
                String lineType = attrs.getValue( ATR_LINETYPE );
                float str = 1.0f;
                if ( stroke != null ) {
                    str = Float.parseFloat( stroke );
                }
                int tr = getAlpha( attrs );
                float lt = 0.0f;
                if ( lineType != null ) {
                    lt = Float.parseFloat( lineType );
                }

                Rect newRect = new Rect( Integer.parseInt( x ), Integer.parseInt( y ), Integer.parseInt( width ),
                        Integer.parseInt( height ), Integer.parseInt( color ), Boolean.valueOf( filled ).booleanValue(), str, tr, lt );

                newGraphics.addShape( newRect );
            } else if ( element.equals( "oval" ) ) {//done
                String x = attrs.getValue( ATR_X );
                String y = attrs.getValue( ATR_Y );
                String width = attrs.getValue( ATR_WIDTH );
                String height = attrs.getValue( ATR_HEIGHT );
                String color = attrs.getValue( ATR_COLOUR );
                String filled = attrs.getValue( ATR_FILLED );
                String stroke = attrs.getValue( ATR_STROKE );
                String lineType = attrs.getValue( ATR_LINETYPE );
                float str = 1.0f;
                if ( stroke != null ) {
                    str = Float.parseFloat( stroke );
                }
                int tr = getAlpha( attrs );
                float lt = 0.0f;
                if ( lineType != null ) {
                    lt = Float.parseFloat( lineType );
                }

                Oval newOval = new Oval( Integer.parseInt( x ), Integer.parseInt( y ), Integer.parseInt( width ),
                        Integer.parseInt( height ), Integer.parseInt( color ), Boolean.valueOf( filled ).booleanValue(), str, tr, lt );

                newGraphics.addShape( newOval );
            } else if ( element.equals( "arc" ) ) {//done
                String x = attrs.getValue( ATR_X );
                String y = attrs.getValue( ATR_Y );
                String width = attrs.getValue( ATR_WIDTH );
                String height = attrs.getValue( ATR_HEIGHT );
                String startAngle = attrs.getValue( ATR_START_ANGLE );
                String arcAngle = attrs.getValue( ATR_ARC_ANGLE );
                String color = attrs.getValue( ATR_COLOUR );
                String filled = attrs.getValue( ATR_FILLED );
                String stroke = attrs.getValue( ATR_STROKE );
                String lineType = attrs.getValue( ATR_LINETYPE );
                float str = 1.0f;
                if ( stroke != null ) {
                    str = Float.parseFloat( stroke );
                }
                int tr = getAlpha( attrs );
                float lt = 0.0f;
                if ( lineType != null ) {
                    lt = Float.parseFloat( lineType );
                }

                Arc newArc = new Arc( Integer.parseInt( x ), Integer.parseInt( y ), Integer.parseInt( width ), Integer.parseInt( height ),
                        Integer.parseInt( startAngle ), Integer.parseInt( arcAngle ), Integer.parseInt( color ), Boolean.valueOf( filled )
                                .booleanValue(), str, tr, lt );

                newGraphics.addShape( newArc );
            } else if ( element.equals( EL_BOUNDS ) ) {//done
                String x = attrs.getValue( ATR_X );
                String y = attrs.getValue( ATR_Y );
                String width = attrs.getValue( ATR_WIDTH );
                String height = attrs.getValue( ATR_HEIGHT );

                newGraphics.setBounds( Integer.parseInt( x ), Integer.parseInt( y ), Integer.parseInt( width ), Integer.parseInt( height ) );
            }
        }

        private Text makeText( Attributes attrs, ClassGraphics graphics ) {
            String str = attrs.getValue( ATR_STRING );
            int colorInt = Integer.parseInt( attrs.getValue( ATR_COLOUR ) );
            // parse the coordinates and check if they are fixed or reverse
            // fixed
            String val = attrs.getValue( ATR_X );
            int x, y, fixedX = 0, fixedY = 0;
            if ( val.endsWith( VAL_RF ) ) {
                x = graphics.getBoundWidth();
                fixedX = x - Integer.parseInt( val.substring( 0, val.length() - 2 ) );
            } else if ( val.endsWith( VAL_F ) ) {
                x = Integer.parseInt( val.substring( 0, val.length() - 1 ) );
                fixedX = -1;
            } else {
                x = Integer.parseInt( val );
            }
            val = attrs.getValue( ATR_Y );
            if ( val.endsWith( VAL_RF ) ) {
                y = graphics.getBoundWidth();
                fixedY = y - Integer.parseInt( val.substring( 0, val.length() - 2 ) );
            } else if ( val.endsWith( VAL_F ) ) {
                y = Integer.parseInt( val.substring( 0, val.length() - 1 ) );
                fixedY = -1;
            } else {
                y = Integer.parseInt( val );
            }

            String fontName = attrs.getValue( ATR_FONTNAME );
            int fontStyle = Integer.parseInt( attrs.getValue( ATR_FONTSTYLE ) );
            int fontSize = Integer.parseInt( attrs.getValue( ATR_FONTSIZE ) );

            Font font = new Font( fontName, fontStyle, fontSize );
            String s = attrs.getValue( ATR_FIXEDSIZE );
            boolean fixed = false;
            fixed = Boolean.valueOf( s ).booleanValue();

            int alpha = getAlpha( attrs );

            Text newText = new Text( x, y, font, new Color( colorInt ), alpha, str, fixed );
            newText.setFixedX( fixedX );

            newText.setFixedY( fixedY );
            return newText;
        }

        private void makeInitialPolygon( Attributes attrs ) {
            polyXs = new ArrayList<String>();
            polyYs = new ArrayList<String>();
            String colour = attrs.getValue( ATR_COLOUR );
            String filled = attrs.getValue( ATR_FILLED );
            String stroke = attrs.getValue( ATR_STROKE );
            String lineType = attrs.getValue( ATR_LINETYPE );
            float str = 1.0f;
            if ( stroke != null ) {
                str = Float.parseFloat( stroke );
            }
            int tr = getAlpha( attrs );
            float lt = 0.0f;
            if ( lineType != null ) {
                lt = Float.parseFloat( lineType );
            }
            polygon = new Polygon( Integer.parseInt( colour ), Boolean.valueOf( filled ).booleanValue(), str, tr, lt );
        }

        private Line makeLine( Attributes attrs, ClassGraphics graphics ) {
            int x1, x2, y1, y2;
            int fixedX1 = 0, fixedX2 = 0, fixedY1 = 0, fixedY2 = 0;
            // parse the coordinates and check if they are fixed or reverse
            // fixed
            String val = attrs.getValue( ATR_X1 );
            if ( val.endsWith( VAL_RF ) ) {
                x1 = graphics.getBoundWidth();
                fixedX1 = x1 - Integer.parseInt( val.substring( 0, val.length() - 2 ) );
            } else if ( val.endsWith( VAL_F ) ) {
                x1 = Integer.parseInt( val.substring( 0, val.length() - 1 ) );
                fixedX1 = -1;
            } else {
                x1 = Integer.parseInt( val );
            }
            val = attrs.getValue( ATR_X2 );
            if ( val.endsWith( VAL_RF ) ) {
                x2 = graphics.getBoundWidth();
                fixedX2 = x2 - Integer.parseInt( val.substring( 0, val.length() - 2 ) );
            } else if ( val.endsWith( VAL_F ) ) {
                x2 = Integer.parseInt( val.substring( 0, val.length() - 1 ) );
                fixedX2 = -1;
            } else {
                x2 = Integer.parseInt( val );
            }
            val = attrs.getValue( ATR_Y1 );
            if ( val.endsWith( VAL_RF ) ) {
                y1 = graphics.getBoundHeight();
                fixedY1 = y1 - Integer.parseInt( val.substring( 0, val.length() - 2 ) );
            } else if ( val.endsWith( VAL_F ) ) {
                y1 = Integer.parseInt( val.substring( 0, val.length() - 1 ) );
                fixedY1 = -1;
            } else {
                y1 = Integer.parseInt( val );
            }
            val = attrs.getValue( ATR_Y2 );
            if ( val.endsWith( VAL_RF ) ) {
                y2 = graphics.getBoundHeight();
                fixedY2 = y2 - Integer.parseInt( val.substring( 0, val.length() - 2 ) );
            } else if ( val.endsWith( VAL_F ) ) {
                y2 = Integer.parseInt( val.substring( 0, val.length() - 1 ) );
                fixedY2 = -1;
            } else {
                y2 = Integer.parseInt( val );
            }
            String color = attrs.getValue( ATR_COLOUR );
            String stroke = attrs.getValue( ATR_STROKE );
            String lineType = attrs.getValue( ATR_LINETYPE );
            float str = 1.0f;
            if ( stroke != null ) {
                str = Float.parseFloat( stroke );
            }
            int tr = getAlpha( attrs );
            float lt = 0.0f;
            if ( lineType != null ) {
                lt = Float.parseFloat( lineType );
            }

            Line newLine = new Line( x1, y1, x2, y2, Integer.parseInt( color ), str, tr, lt );
            newLine.setFixedX1( fixedX1 );
            newLine.setFixedX2( fixedX2 );
            newLine.setFixedY1( fixedY1 );
            newLine.setFixedY2( fixedY2 );
            return newLine;
        }

        @Override
        public void endElement( String namespaceURI, String sName, String qName ) {

            if ( qName.equals( EL_CLASS ) ) {
                pack.getClasses().add( newClass );
            } else if ( qName.equals( EL_PORT ) ) {//done
                if ( newPort.getOpenGraphics() == null ) {
                    newGraphics = new ClassGraphics();
                    newGraphics.addShape( new Oval( -4, -4, 8, 8, 12632256, true, 1.0f, 255, 0, true ) );
                    newGraphics.addShape( new Oval( -4, -4, 8, 8, 0, false, 1.0f, 255, 0, true ) );

                    newGraphics.setBounds( -4, -4, 8, 8 );
                    newPort.setOpenGraphics( newGraphics );
                }
                if ( newPort.getClosedGraphics() == null ) {
                    newGraphics = new ClassGraphics();
                    newGraphics.addShape( new Oval( -4, -4, 8, 8, 0, true, 1.0f, 255, 0, true ) );
                    newGraphics.setBounds( -4, -4, 8, 8 );
                    newPort.setClosedGraphics( newGraphics );
                }

                newClass.addPort( newPort );
                status = CLASS;
            } else if ( qName.equals( EL_FIELDS ) ) {
                status = CLASS;
            } else if ( qName.equals( EL_POLYGON ) ) {
                // arrays of polygon points
                int[] xs = new int[polyXs.size()];
                int[] ys = new int[polyYs.size()];
                // arrays of FIXED information about polygon points
                int[] fxs = new int[polyXs.size()];
                int[] fys = new int[polyYs.size()];

                for ( int i = 0; i < polyXs.size(); i++ ) {
                    String s = polyXs.get( i );
                    // parse the coordinates and check if they are fixed or
                    // reverse fixed
                    if ( s.endsWith( VAL_RF ) ) {
                        xs[i] = newGraphics.getBoundWidth();
                        fxs[i] = newGraphics.getBoundWidth() - Integer.parseInt( s.substring( 0, s.length() - 2 ) );
                    } else if ( s.endsWith( VAL_F ) ) {
                        xs[i] = Integer.parseInt( s.substring( 0, s.length() - 1 ) );
                        fxs[i] = -1;
                    } else {
                        xs[i] = Integer.parseInt( s );
                        fxs[i] = 0;
                    }
                    s = polyYs.get( i );
                    if ( s.endsWith( VAL_RF ) ) {
                        ys[i] = newGraphics.getBoundHeight();
                        fys[i] = newGraphics.getBoundHeight() - Integer.parseInt( s.substring( 0, s.length() - 2 ) );
                    } else if ( s.endsWith( VAL_F ) ) {
                        ys[i] = Integer.parseInt( s.substring( 0, s.length() - 1 ) );
                        fys[i] = -1;
                    } else {
                        ys[i] = Integer.parseInt( s );
                        fys[i] = 0;
                    }
                }
                polygon.setPoints( xs, ys, fxs, fys );
                newGraphics.addShape( polygon );
            } else if ( qName.equals( EL_GRAPHICS ) ) {

                if ( status == FIELD ) {//done
                    if ( newField != null ) {
                        newField.setDefaultGraphics( newGraphics );
                    } else {
                        collector.collectDiagnostic( "Default Graphics ignored" );
                    }
                } else if ( status == FIELD_KNOWN ) {//done
                    if ( newField != null ) {
                        newField.setKnownGraphics( newGraphics );
                    } else {
                        collector.collectDiagnostic( "Known Graphics ignored" );
                    }
                } else if ( status == PORT_OPEN ) {//done
                    newPort.setOpenGraphics( newGraphics );
                } else if ( status == PORT_CLOSED ) {//done
                    newPort.setClosedGraphics( newGraphics );
                } else {
                    // newGraphics.packageClass = newClass;
                    newClass.addGraphics( newGraphics );//done
                }
            } else if ( EL_PAINTER.equals( element ) ) {//done
                newClass.setPainterName( charBuf.toString() );
                pack.setPainters( true );
            } else if ( EL_ICON.equals( element ) ) {//done
                newClass.setIcon( charBuf.toString() );
            } else if ( EL_DESCRIPTION.equals( element ) ) {//done
                if ( status == PACKAGE )
                    pack.setDescription( charBuf.toString() );
                else
                    newClass.setDescription( charBuf.toString() );
            } else if ( EL_NAME.equals( element ) ) {//done
                // if we are reading a package field
                if ( status == PACKAGE )
                    pack.setName( charBuf.toString() );//done
                else {// else we a reading a class field
                    newClass.setName( charBuf.toString() );//done

                    if(newClass.getComponentType() != PackageClass.ComponentType.SCHEME) {//done
                        Collection<ClassField> specFields;

                        try {
                            specFields = SpecParser.getFields( path, newClass.getName(), ".java" );
                            newClass.setSpecFields( specFields );

                        } catch ( IOException e ) {

                            collector.collectDiagnostic( "Class " + newClass.getName() + " specified in package does not exist." );
                        } catch ( SpecParseException e ) {
                            collector.collectDiagnostic( "Unable to parse the specification of class " + newClass.getName() );
                        }
                    }
                }
            }

            charBuf.delete( 0, charBuf.length() );
        }

        @Override
        public void characters( char[] buf, int offset, int len ) {
            // Characters may be delivered in multiple chunks so we just
            // accumulate them here.
            charBuf.append( buf, offset, len );
        }

        /**
         * Returns alpha value specified in the attributes.
         * If the relevant attribute value is missing the default value
         * is returned.
         * @param attrs attributes read from XML
         * @return alpha value
         */
        private int getAlpha( Attributes attrs ) {
            String alphaStr = attrs.getValue( ATR_TRANSPARENCY );
            if ( alphaStr != null ) {
                try {
                    return Integer.parseInt( alphaStr );
                } catch ( NumberFormatException e ) {
                    db.p( e );
                }
            }
            return ALPHA_IMPLIED;
        }
    }

    /**
     * Parse the Java file, read the specification and make the relations
     * accordingly.
     * 
     * @return VPackage -
     */
    public VPackage getPackage() {
        return pack;
    } // getPackage

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    public static VPackage loadPackage_(File f) {

        if ( f != null && f.exists()) {

            try {

                String packageName = f.getName().substring( 0, f.getName().indexOf( "." ) );

                PackageParser loader = new PackageParser();
                boolean isOK = false;

                if ( loader.load( f ) ) {
                    if ( loader.getDiagnostics().hasProblems() ) {
                        if ( DiagnosticsCollector.promptLoad( Editor.getInstance(), loader.getDiagnostics(), "Warning: Package " + packageName + " contains errors", "package" ) ) {
                            isOK = true;
                        }
                    } else {
                        isOK = true;
                    }
                } else {
                    List<String> msgs = loader.getDiagnostics().getMessages();
                    String msg;
                    if ( msgs.size() > 0 )
                        msg = msgs.get( 0 );
                    else
                        msg = "An error occured. See the log for details.";

                    JOptionPane.showMessageDialog( Editor.getInstance(), msg, "Error loading package", JOptionPane.ERROR_MESSAGE );
                }

                if( isOK ) {
                    return loader.getPackage();
                }
            } catch ( Exception e ) {
                String message = "Unable to load package \"" + f.getAbsolutePath() + "\"";
                db.p( message );
                if ( RuntimeProperties.isLogDebugEnabled() ) {
                    e.printStackTrace( System.out );
                }
                JOptionPane.showMessageDialog( Editor.getInstance(), message, "Error", JOptionPane.ERROR_MESSAGE );
            }
        }
        return null;
    }
}
