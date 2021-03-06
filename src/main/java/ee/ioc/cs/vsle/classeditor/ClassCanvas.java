package ee.ioc.cs.vsle.classeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import ee.ioc.cs.vsle.editor.CodeViewer;
import ee.ioc.cs.vsle.editor.ProgramRunnerEvent;
import ee.ioc.cs.vsle.editor.RuntimeProperties;
import ee.ioc.cs.vsle.editor.State;
import ee.ioc.cs.vsle.event.EventSystem;
import ee.ioc.cs.vsle.graphics.BoundingBox;
import ee.ioc.cs.vsle.graphics.Image;
import ee.ioc.cs.vsle.graphics.Line;
import ee.ioc.cs.vsle.graphics.Shape;
import ee.ioc.cs.vsle.graphics.Text;
import ee.ioc.cs.vsle.vclass.Canvas;
import ee.ioc.cs.vsle.vclass.ClassObject;
import ee.ioc.cs.vsle.vclass.ClassPainter;
import ee.ioc.cs.vsle.vclass.Connection;
import ee.ioc.cs.vsle.vclass.GObj;
import ee.ioc.cs.vsle.vclass.PackageClass;
import ee.ioc.cs.vsle.vclass.Point;
import ee.ioc.cs.vsle.vclass.Port;
import ee.ioc.cs.vsle.vclass.RelObj;
import ee.ioc.cs.vsle.vclass.VPackage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassCanvas extends Canvas{

	private static final long serialVersionUID = 1290323197462938186L;
	private static final Logger logger = LoggerFactory.getLogger(ClassCanvas.class);
	

	private static final int MINIMUM_STEP = 3; /*  SHAPES will not be resized smaller than this  */
	
	public MouseOps mListener; 
    private boolean drawOpenPorts = true;    
    public IconPalette iconPalette;
	public BoundingBox boundingBox;	
	public List<Port> portList;
	// Class properties.
	//current
	private ClassObject classObject;
		

    public  ClassObject getClassObject() {
		return classObject;
	}

	public void setClassObject(ClassObject classObject) {
		this.classObject = classObject;
	}


	public ClassCanvas( VPackage _package, String workingDir ) {    	
    	 super(workingDir);    	 
    	 vPackage = _package;
         m_canvasTitle = vPackage.getName();
         initialize();
    }
	
    
    @Override
    protected void initialize() {
    	super.initialize();
        mListener = new MouseOps( this );     
        drawingArea.addMouseListener( mListener );
        drawingArea.addMouseMotionListener( mListener );
    }
    
    public void updateBoundingBox(){
    	GObj bbObj = null;
    	for (GObj obj : getObjectList()) {
            for (Shape shape : obj.getShapes()) {
				if (shape instanceof BoundingBox) {
					bbObj = obj;
				} 								 
			}           
        }    	
    	if(bbObj != null){
    		 for (Shape shape : bbObj.getShapes()) {
 				if (shape instanceof Text) {
 					((Text) shape).setText(getTextForBoundingBox());
 				} 								 
 			}  
    		repaint();
    	}
    }
    
    public String getTextForBoundingBox(){
    	String text = "ClassNameNotDefined";
    	if (classObject != null && classObject.getClassName() != null &&  classObject.getClassName() != ""){
    		text =  classObject.getClassName();
    	}
    	return text;
    }
    
    public Shape drawTextForBoundingBox(int x, int y){    	
    	return drawTextForBoundingBox(x, y, getTextForBoundingBox());
    }
    
    
    public void clearObjectsWarning() {
    	
    	/* Warning that fields will be cleared as well */
    	int clear = JOptionPane.showConfirmDialog( null, "Clear Working Area? \n Please note that all existing fields will be deleted.",  null, JOptionPane.OK_CANCEL_OPTION  );					  
		  if ( clear == JOptionPane.YES_OPTION ) {
			  this.clearObjects();	
			  // clear fields
			  getClassObject().removeClassFieldsGraphics();
		  } else if (clear == JOptionPane.CANCEL_OPTION){
			  return;
		  }
    }
    
    public void clearObjects() {
    	
			  super.clearObjects();
		    	if(iconPalette != null && iconPalette.boundingbox.isSelected()){
		    		iconPalette.selection.setSelected(true);
		    		iconPalette.boundingbox.setSelected(false);
		    	}
		        drawingArea.repaint();		  
    }
    
    public Shape drawTextForBoundingBox(int x, int y, String text){    
    			 
    	/* Magic number to position text on BB */
    			 if(x < 100){
    				 x = x/2;
    			 } else {
    				 x = x - 100;
    			 }    			 
    	    	 Shape sText = new Text( x, y + 15,
    			 new Font("Arial", Font.BOLD, 13), Color.black, text);
    	return sText;
    }
    
    @Override
    public DrawingArea getDrawingArea() {
    	drawingArea = new DrawingArea();
    	drawingArea.init();
        return  (DrawingArea) drawingArea;
    }

    public class DrawingArea extends Canvas.DrawingArea {
       
		private static final long serialVersionUID = 7866172800421330602L;

		@Override        
        protected void paintComponent( Graphics g ) {
			super.paintComponent(g);
		        Graphics2D g2 = (Graphics2D) g;
		        
		        //     hide or show BoundingBox
		        if(iconPalette != null){
		        	iconPalette.boundingbox.setEnabled( !isBBPresent() );
		        }
		        
		        // coordinates
		        
		          int rectX = (Math.min(Math.abs((int)(mListener.startX*getScale())), mouseX ));
		          int rectY = (Math.min(Math.abs((int)(mListener.startY*getScale())), mouseY ));
		          int width = Math.abs( mouseX - (Math.abs((int)(mListener.startX*getScale()))));
		          int height = Math.abs( mouseY - (Math.abs((int)(mListener.startY*getScale()))));
		        
		        // Draw Shapes
		                
		         if ( mListener.state.equals( State.drawArc1 ) ) {
		        	 //int mx = Math.abs((int)(mListener.startX*getScale()));				        	      
		        	 //int my = Math.abs((int)(mListener.startY*getScale()));
		        	 int mx = Math.abs((int)(mListener.arcStartX*getScale()));
		        	 int my = Math.abs((int)(mListener.arcStartY*getScale()));
		        	 int mw = Math.abs((int)(mListener.arcWidth*getScale()));		        	
		        	 int mh = Math.abs((int)(mListener.arcHeight*getScale()));
		             g.drawRect(mx, my, mw,  mh);
		             g.drawLine( mx + mw / 2, my + mh / 2, (int) Math.abs(mouseX*getScale()),
		            		 (int) Math.abs(mouseY*getScale()) );			             
		         } else if ( mListener.state.equals( State.drawArc2 ) ) {
		        	 int mx = Math.abs((int)(mListener.arcStartX*getScale()));		     
		        	 int my = Math.abs((int)(mListener.arcStartY*getScale()));
		        	 int mw = Math.abs((int)(mListener.arcWidth*getScale()));		        	
		        	 int mh = Math.abs((int)(mListener.arcHeight*getScale()));
		             if ( mListener.fill ) {
		            	 
		            	 g2.fillArc( mx, my, mw, mh, mListener.arcStartAngle, mListener.arcAngle );

		             } else {
		            	 g2.drawArc( mx, my, mw, mh, mListener.arcStartAngle, mListener.arcAngle );		                
		             }

			
		}	
		         if ( !mListener.mouseState.equals( "released" ) ) {

		             if ( mListener.state.equals( State.dragBox ) 
		                     || mListener.state.equals( State.boundingbox )) {
		                    g2.setColor( Color.gray );
		                    g2.drawRect( rectX, rectY, width, height );
		                } else {
		                    
		                    int red = mListener.color.getRed();
		                    int green = mListener.color.getGreen();
		                    int blue = mListener.color.getBlue();

		                    int alpha = mListener.getTransparency();
		                    g2.setColor( new Color( red, green, blue, alpha ) );

		                    if ( mListener.lineType > 0 ) {
		                        g2.setStroke( new BasicStroke( mListener.strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
		                                50, new float[] { mListener.lineType, mListener.lineType }, 0 ) );
		                    } else {
		                        g2.setStroke( new BasicStroke( mListener.strokeWidth ) );
		                    }
		            
		                    if ( mListener.state.equals( State.drawRect ) ) {
		                        g2.setColor( mListener.color );
		                        g2.drawRect( rectX, rectY, width, height );
		                    } else if ( mListener.state.equals( State.drawFilledRect ) ) {
		                        g2.setColor( mListener.color );
		                        g2.fillRect( rectX, rectY, width, height );    
		                 } else if ( mListener.state.equals( State.drawLine ) ) {
		                	 int sx = Math.abs((int)(mListener.startX*getScale()));
		                	 int sy = Math.abs((int)(mListener.startY*getScale()));
		                     g2.drawLine(sx, sy, mouseX, mouseY );                        
		                    } else if ( mListener.state.equals( State.drawOval ) ) {
		                        g2.setColor( mListener.color );
		                        g2.drawOval( rectX, rectY, width, height );
		                    } else if ( mListener.state.equals( State.drawFilledOval ) ) {
		                        g2.setColor( mListener.color );
		                        g2.fillOval( rectX, rectY, width, height );
		                 } else if ( mListener.state.equals( State.drawArc ) ) {
		                     g.drawRect( rectX, rectY, width, height );		                   
		                 } else if ( mListener.state.equals( State.drawFilledArc ) ) {
		                     g.drawRect( rectX, rectY, width, height );
		        }        

		                }
		         }
		}
    }
    public void setDrawOpenPorts(boolean drawOpenPorts) {
		
		this.drawOpenPorts = drawOpenPorts;		
		for (GObj obj : scheme.getObjectList()) {
		    obj.setDrawOpenPorts( drawOpenPorts );
        }
		drawingArea.repaint();
	}
    
    public boolean isDrawOpenPorts() {
		return drawOpenPorts;
	}
    
    public BoundingBox getBoundingBox() {
		return boundingBox;
	}
    
    public boolean isBBPresent() {
        boolean isBbPresent = false;
        for (GObj obj : getObjectList()) {
            for (Shape shape : obj.getShapes()) {
				if (shape instanceof BoundingBox) {
					isBbPresent = true;
					boundingBox = new BoundingBox(obj.getX(),obj.getY(), shape.getWidth(), shape.getHeight());					
					break;
				}
			}
        }
        return isBbPresent;
    }
    
    public boolean isBBTop() {
        boolean isBbTop = false;
        for (GObj obj : getObjectList()) {
            for (Shape shape : obj.getShapes()) {
				if (shape instanceof BoundingBox) {
				   if(getObjectList().indexOf(obj) == getObjectList().size() - 1)
					   isBbTop = true;
					}			
					break;
				}
			}
         return isBbTop;
    }
    
    @Override
    public void openClassCodeViewer( String className ) {
    	if (className == null){
    		  JOptionPane.showMessageDialog(this, 
                      "View Code failed: no class name\n" +
                      "\nYou may need to revise the application settings.",
                      "Error Running External Editor",
                      JOptionPane.ERROR_MESSAGE);
    		return;
    	}
        String editor = RuntimeProperties.getDefaultEditor();        
        if (editor == null) {
            CodeViewer cv = new CodeViewer(className, RuntimeProperties.getLastPath()); /* java file is in same dir as Package*/
            cv.setLocationRelativeTo( ClassEditor.getInstance() );
            cv.open();
       } else {
       // if(editor != null && className != null) {
            File wd = new File(getWorkDir());
            String editCmd = editor.replace("%f",
                    new File(className + ".java").getPath());

            try {
                Runtime.getRuntime().exec(editCmd, null, wd);
            } catch (IOException ex) {
                if(logger.isDebugEnabled()) {
                    logger.error(null, ex);
                }
                JOptionPane.showMessageDialog(this,
                        "Execution of the command \"" + editCmd
                        + "\" failed:\n" + ex.getMessage() +
                        "\nYou may need to revise the application settings.",
                        "Error Running External Editor",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
	public void finalizeResizeObjects() {
		for (GObj obj : scheme.getObjectList()) {
			if (obj.isSelected()) {
				if (obj.getXsize() != (float) 1) {
					int calcWidth = (int) (obj.getWidth() * obj.getXsize());
					for (Shape s : obj.getShapes()) {
						if (s instanceof Line) {
							if(((Line) s).getX() < ((Line) s).getEndX()){								
								((Line) s).setEndX(Math.abs(calcWidth));								
							} else {							
								((Line) s).setX(Math.abs(calcWidth));
								((Line) s).setEndX(0);
							}
				    	   	if (logger.isDebugEnabled()) {
								logger.debug("calc w {}", calcWidth);
				    		}
							obj.setWidth(Math.abs(calcWidth));
							s.setWidth(Math.abs(calcWidth));
							if(calcWidth < 0 && obj.getX() > ((Line) s).getX()){
								obj.setX(obj.getX() - obj.getWidth());	
								((Line) s).flip();
							} 							
						} else {
							s.setWidth(calcWidth >= MINIMUM_STEP ? calcWidth : MINIMUM_STEP);
							obj.setWidth(calcWidth >= MINIMUM_STEP ? calcWidth : MINIMUM_STEP);
						}
					}
					obj.setXsize((float) 1);
				}
				if (obj.getYsize() != (float) 1) {
					for (Shape s : obj.getShapes()) {
						int calcHeight = (int) (obj.getHeight() * obj.getYsize());
						if (s instanceof Line) { // check that height will not
													// be negative
							int newHeight;
							if (calcHeight < 0){
								newHeight = Math.abs(calcHeight);
								obj.setY(obj.getY() - newHeight);
								if(obj.getYsize() < 0){
									((Line) s).flip();
								}
								//if(obj.getY() < obj.get)
							}
							else if (s.getWidth() == 0){
								newHeight = calcHeight > 0 ? calcHeight : 1; // at least one dimension
								 											// has to be  >0
								((Line) s).setEndY(newHeight);
							}												
							else
								newHeight = calcHeight;
							((Line) s).setEndY(newHeight);
							s.setHeight(newHeight);
							obj.setHeight(newHeight);
						} else {
							s.setHeight(calcHeight  >= MINIMUM_STEP  ? calcHeight :MINIMUM_STEP );
							obj.setHeight(calcHeight  >= MINIMUM_STEP  ? calcHeight :  MINIMUM_STEP );
						}
						obj.setYsize((float) 1);
					}
				}
			}
		}
		scheme.getObjectList().updateRelObjs();
		drawingArea.repaint();
	}
	
	 public void cloneObject() {
	        ArrayList<GObj> newObjects = new ArrayList<GObj>();
	        Map<GObj, ClassPainter> newPainters = null;

	        // clone every selected object
	        for (GObj obj : scheme.getSelectedObjects()) {
	        	if(obj.getShapes().get(0) != null && obj.getShapes().get(0) instanceof BoundingBox){
	        		obj.setSelected( false );
	        		continue; // NO CLONING for BB
	        	}  
	        	if(obj.getShapes().get(0) != null && obj.getShapes().get(0).isField()){
	        		obj.setSelected( false );
	        		continue; // NO CLONING for fields
	        	}  
	            GObj newObj = obj.clone();
	            if (obj.getName().equals("port")) {
	            	String newName = JOptionPane.showInputDialog(this, "Cloned Port Name:");  
	            	if(newName == null || newName == ""){
	            		newName = "(cloned port)";
	            	}
	            	if(newObj.getPortList().get(0) != null){
	            		newObj.getPortList().get(0).setName(newName);            		
	            	} 
	            }
	            newObj.setPosition( newObj.getX() + 20, newObj.getY() + 20 );
	            newObjects.addAll( newObj.getComponents() );

	            // create new and fresh class painter for cloned object
	            if ( vPackage.hasPainters() &&  obj.getClassName() != null) {
	                PackageClass pc = vPackage.getClass( obj.getClassName() );
	                ClassPainter painter = pc.getPainterFor( scheme, newObj );
	                if ( painter != null ) {
	                    if ( newPainters == null )
	                        newPainters = new HashMap<GObj, ClassPainter>();
	                    newPainters.put( newObj, painter );
	                }
	            }

	            obj.setSelected( false );
	        }

	        for ( GObj obj : newObjects ) {
	            if ( obj instanceof RelObj ) {
	                RelObj robj = (RelObj) obj;
	                for ( GObj obj2 : newObjects ) {
	                    if ( robj.getStartPort().getObject().getName().equals( obj2.getName() ) ) {
	                        robj.getStartPort().setObject( obj2 );
	                    }
	                    if ( robj.getEndPort().getObject().getName().equals( obj2.getName() ) ) {
	                        robj.getEndPort().setObject( obj2 );
	                    }
	                }
	            }
	        }

	        // now the hard part - we have to clone all the connections
	        ArrayList<Connection> newConnections = new ArrayList<Connection>();
	        for ( Connection con : scheme.getConnectionList() ) {
	            GObj beginObj = null;
	            GObj endObj = null;

	            for ( GObj obj : newObjects ) {
	                if ( obj.getName().equals( con.getBeginPort().getObject().getName() ) )
	                    beginObj = obj;
	                if ( obj.getName().equals( con.getEndPort().getObject().getName() ) )
	                    endObj = obj;

	                if ( beginObj != null && endObj != null ) {
	                    Connection newCon = new Connection( beginObj.getPortList().get( con.getBeginPort().getNumber() ), endObj.getPortList()
	                            .get( con.getEndPort().getNumber() ), con.isStrict() );

	                    for ( Point p : con.getBreakPoints() )
	                        newCon.addBreakPoint( new Point( p.x + 20, p.y + 20 ) );

	                    newConnections.add( newCon );
	                    break;
	                }
	            }
	        }

	        getObjectList().addAll( newObjects );

	        // New connections have to be added after the new objects have been
	        // committed or new ports will not get connected properly.
	        scheme.getConnectionList().addAll( newConnections );

	        if ( classPainters != null && newPainters != null )
	            classPainters.putAll( newPainters );

	        undoSupport.postEdit( new CloneEdit( this, newObjects, newConnections, newPainters ) );

	        drawingArea.repaint();
	    }

	 public void rotateMany(double angle){
		 if(getObjectList().getSelectedCount() != 0 ) {
				for(GObj o:getObjectList().getSelected()){
					rotateOne(o, angle);
				}
		 }		 
	 }
	 public void rotateOne(GObj obj, double angle){
		 obj.setAngle(  obj.getAngle() + Math.toRadians( angle ) );
	 }
	
	 public void resizeLine( int dx, int dy, int corner ) {
		if (logger.isDebugEnabled()) {
			logger.debug("resizeLine dx={}, dy={}, corner={}", dx, dy, corner);
		}
		
		for (GObj obj : scheme.getObjectList()) {
		    if ( obj.isSelected() ){	            	  
		        obj.resizeLine( dx, dy, corner );
		    }
		}
		scheme.getObjectList().updateRelObjs();
		drawingArea.repaint();
	 }
	 
	 @Override
	 public void resizeObjects( int dx, int dy, int corner ) {
	        for (GObj obj : scheme.getObjectList()) {
	            if ( obj.isSelected() )
	            	for( Shape s : obj.getShapes()){
	                 	if(s instanceof Line){
	                 		resizeLine(dx, dy, corner);
	                 	}
	                 	else 	
	                obj.resize( dx, dy, corner );
	            	}
	        }
	        scheme.getObjectList().updateRelObjs();
	        drawingArea.repaint();
	    }
	 
	 
    /**
     * Sets actionInProgress. Actions that consist of more than one atomic step
     * that cannot be interleaved with other actions should set this property
     * and unset it after completion. For example, consider this scenario:
     * <ol>
     * <li>a new object is created</li>
     * <li>a new connection is connected to the new object</li>
     * <li>before connecting a second object the addition of the object is
     * undone</li>
     * </ol>
     * This is a case when undo-redo should be disabled until either the
     * connection is cancelled or the second end is connected.
     * 
     * @param newValue the actionInProgress value
     */
    public void setActionInProgress( boolean newValue ) {
        if ( newValue != actionInProgress ) {
            actionInProgress = newValue;
            ClassEditor editor = ClassEditor.getInstance();
            editor.deleteAction.setEnabled( !newValue );
            editor.refreshUndoRedo();
        }
    }
    
    @Override
    public void destroy() {
        
        for ( long id : super.m_runners ) {
            ProgramRunnerEvent event = new ProgramRunnerEvent( this, id, ProgramRunnerEvent.DESTROY );

            EventSystem.queueEvent( event );
        }
        
        iconPalette.destroy();
        iconPalette = null;
        
        drawingArea.removeMouseListener( mListener );
        drawingArea.removeMouseMotionListener( mListener );
        drawingArea.removeKeyListener( keyListener );
        drawingArea.removeAll();
        drawingArea.setFocusable( false );
        drawingArea = null;
        areaScrollPane.removeAll();
        areaScrollPane = null;
        removeAll();
        
        mListener.destroy();
        mListener = null;
        
        this.removeKeyListener( keyListener );
        keyListener.destroy();
        keyListener = null;
        
        if( scheme != null ) {
            scheme.destroy();
            scheme = null;
        }
        vPackage = null;
        super.classPainters = null;
        currentPainter = null;
        currentObj = null;
        currentCon = null;
        drawAreaSize = null;
        infoPanel = null;
        posInfo = null;
        backgroundImage = null;
        
        executor.shutdownNow();
        executor = null;
        undoManager = null;
        undoSupport = null;
    }
    
    
    /**
     * Starts adding a new relation object.
     * 
     * @param port the first port of the relation object
     */
    protected void startAddingRelObject( Port port ) {    	
    	super.startAddingRelObject(port);
        setActionInProgress( true );
    }

    protected void cancelAddingObject() {
    	super.cancelAddingObject();
        setActionInProgress( false );
    }
    

    /**
     * Update mouse position in info label
     */
    public void setPosInfo( int x, int y ) {
        String message = x + ", " + y;

        GObj obj = getObjectList().checkInside(x, y, 1);

        if( obj != null ) {
            
            message += " " + obj.getMessage();
            
        }
        
        setStatusBarText( message );
    }


    public void openPropertiesDialog( GObj obj ) {

    	if(obj.getShapes() != null && obj.getShapes().get(0) instanceof Text){
   		 	new TextDialog( ClassEditor.getInstance(),obj).setVisible( true );        		
    	} else if (obj.getShapes() != null && obj.getShapes().get(0) instanceof Image){
   		  new ImageDialog( ClassEditor.getInstance(),obj).setVisible( true );
    	} else  new ShapePropertiesDialog(ClassEditor.getInstance(),obj).setVisible( true );      	    	       
    }

   public void removeObjectByName(String name, boolean def){
	   GObj target = null;
	   for(GObj obj:getObjectList()){
		   if(obj.getShapes().size() > 0 && obj.getShapes().get(0).isField() && 
				   obj.getShapes().get(0).getName().equals(name) && obj.getShapes().get(0).isFieldDefault() == def)
			   target = obj;
	   }
	   if(target != null){
		   getObjectList().remove(target);
	   }
   }
   
	public List<Port> getPortList() {
		portList = new ArrayList<Port>();
		for(GObj o :getObjectList()){
			if(o.getPortList().size() > 0){
				portList.addAll(o.getPortList());
			}
		}
		return portList;
	}
       
}
