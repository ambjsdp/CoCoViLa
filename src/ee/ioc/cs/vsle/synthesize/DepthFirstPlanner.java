/**
 * 
 */
package ee.ioc.cs.vsle.synthesize;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import ee.ioc.cs.vsle.editor.*;
import ee.ioc.cs.vsle.util.*;
import static ee.ioc.cs.vsle.synthesize.CodeGenerator.*;

/**
 * This is a singleton class responsible for the whole planning
 * @author pavelg
 * 
 */
public class DepthFirstPlanner implements IPlanner {

	private static DepthFirstPlanner s_planner = null;

	/**
	 * private constructor
	 */
    private DepthFirstPlanner() {}

    /**
     * @return DepthFirstPlanner singleton
     */
    public static DepthFirstPlanner getInstance() {
        if ( s_planner == null ) {
            s_planner = new DepthFirstPlanner();
        }
        return s_planner;
    }

    /**
     * @return description of this planner
     */
    public String getDescription() {
    	return "Depth First";
    }
    
    /**
     * This method is the access point to the planning procedure.
     * Initially, it adds all variables from axioms to the set of found vars, 
     * then does the linear planning. If lp does not solve the problem and there are subtasks,
     * goal-drived recursive planning with backtracking is invoked. Planning is performed until 
     * no new variables are intoduced into the algorithm.
     */
    public ArrayList<Rel> invokePlaning( Problem problem, boolean computeAll ) {
    	long startTime = System.currentTimeMillis();
    	
        ArrayList<Rel> algorithm = new ArrayList<Rel>();
        
        //manage axioms
        for ( Iterator<Rel> axiomIter = problem.getAxioms().iterator(); axiomIter.hasNext(); ) {
            Rel rel = axiomIter.next();
            algorithm.add( rel );
            axiomIter.remove();
        }

        problem.getFoundVars().addAll(problem.getKnownVars());
        
        //invoke linear planning
        if ( linearForwardSearch( problem, algorithm, problem.getGoals(), 
        		// do not optimize yet if subtasks exist
        		( computeAll || !problem.getRelsWithSubtasks().isEmpty() ) ) && !computeAll ) {
        	
        	if ( RuntimeProperties.isLogInfoEnabled() )
    			db.p( "Problem solved without subtasks");
        	
        } else if ( !problem.getRelsWithSubtasks().isEmpty() ) {
        	
        	int foundVarsCount;
        	//do planning until no new vars are computed
        	do {
        		foundVarsCount = problem.getFoundVars().size();
//        		System.out.println( "Before sb " + foundVarsCount + " " + problem.getFoundVars() );
        		subtaskPlanning( problem, algorithm, computeAll );
//        		System.out.println( "After sb " + problem.getFoundVars().size() + " " + problem.getFoundVars() );
        		if( foundVarsCount < problem.getFoundVars().size() )
        		{
        			foundVarsCount = problem.getFoundVars().size();
        			
        			linearForwardSearch( problem, algorithm, problem.getGoals(), computeAll );
//        			System.out.println( "After lp " + problem.getFoundVars().size() + " " + problem.getFoundVars() );
        			if( ( foundVarsCount == problem.getFoundVars().size() ) 
        					|| problem.getFoundVars().containsAll( problem.getGoals() ) ) {
        				break;
        			}
        		}
        	} while( foundVarsCount < problem.getFoundVars().size() );
        }
		if ( RuntimeProperties.isLogInfoEnabled() )
			db.p( "Planning time: " + ( System.currentTimeMillis() - startTime ) + "ms.");
		
        return algorithm;
    }
    
    /**
     * Linear forward search algorithm
     * 
     * @param p
     * @param algorithm
     * @param targetVars
     * @param computeAll
     * @return
     */
	private boolean linearForwardSearch(Problem p, List<Rel> algorithm, 
			Set<Var> targetVars,
			boolean computeAll) {
		
		/*
		 * while iterating through hashset, items cant be removed from/added to
		 * that set. Theyre collected into these sets and added/removedall
		 * together after iteration is finished
		 */
		Set<Var> newVars = new LinkedHashSet<Var>();
		Set<Var> relOutputs = new LinkedHashSet<Var>();
		Set<Var> removableVars = new LinkedHashSet<Var>();
		// backup goals for later optimization
		Set<Var> allTargetVars = new LinkedHashSet<Var>( targetVars );
		
		boolean changed = true;

		if (isLinearLoggingOn())
			db.p("------Starting linear planning with (sub)goals: "
					+ targetVars + "--------");

		if (isLinearLoggingOn())
			db.p("Algorithm " + algorithm);
		
		int counter = 1;

		while ((!computeAll && changed && !targetVars.isEmpty())
				|| (changed && computeAll)) {

			if (isLinearLoggingOn())
				db.p("----Iteration " + counter + " ----");

			counter++;
			changed = false;
			// remove targets if they're already known
			for (Iterator<Var> targetIter = targetVars.iterator(); targetIter.hasNext();) {
				Var targetVar = targetIter.next();
				if (p.getFoundVars().contains(targetVar)) {
					targetIter.remove();
				}
			}
			// iterate through all knownvars
			if (isLinearLoggingOn())
				db.p("Known:" + p.getKnownVars());
			
			for (Var var : p.getKnownVars() ) {
				
				if (isLinearLoggingOn())
					db.p("Current Known: " + var);
				
				// Check the relations of all components
				for (Rel rel : var.getRels() ) {
					if (isLinearLoggingOn())
						db.p("And its rel: " + rel);
					if (p.getAllRels().contains(rel)) {
						if( !var.getField().isConstant() ) {
							rel.removeUnknownInput( var );
						}

						if (isLinearLoggingOn())
							db.p("problem contains it " + rel
									+ " unknownInputs: "
									+ rel.getUnknownInputCount());

						removableVars.add(var);

						if (rel.getUnknownInputCount() == 0
								&& rel.getType() != RelType.TYPE_METHOD_WITH_SUBTASK) {

							if (isLinearLoggingOn())
								db.p("rel is ready to be used " + rel);

							boolean relIsNeeded = false;

							if (isLinearLoggingOn())
								db.p("tema outputsid " + rel.getOutputs());

							for (Var relVar : rel.getOutputs() ) {

								if (!p.getFoundVars().contains(relVar)) {
									relIsNeeded = true;
								}
							}

							if (rel.getOutputs().isEmpty()) {
								relIsNeeded = true;
							}
							if (isLinearLoggingOn())
								db.p("relIsNeeded " + relIsNeeded);

							if (relIsNeeded) {

								if (isLinearLoggingOn())
									db.p("ja vajati " + rel);

								if (!rel.getOutputs().isEmpty()) {
									relOutputs.clear();
									addVarsToSet( rel.getOutputs(), relOutputs );
									newVars.addAll( relOutputs );
									p.getFoundVars().addAll( relOutputs );
								}
								algorithm.add(rel);
								if (isLinearLoggingOn())
									db.p("algorithm " + algorithm);
							}

							p.getAllRels().remove(rel);
							changed = true;
						}
					}
				}
			}

			if (isLinearLoggingOn())
				db.p("foundvars " + p.getFoundVars());

			p.getKnownVars().addAll(newVars);
			p.getKnownVars().removeAll(removableVars);
			newVars.clear();
		}
		if (isLinearLoggingOn())
			db.p("algorithm " + algorithm);

		if (!computeAll) {
			Optimizer.optimize( algorithm, new HashSet<Var>( allTargetVars ) );
			
			if (isLinearLoggingOn())
				db.p("optimized algorithm " + algorithm);
		}

		if (isLinearLoggingOn())
			db.p("\n---!!!Finished linear planning!!!---\n");
		
		return targetVars.isEmpty() || p.getFoundVars().containsAll( allTargetVars );
	}

	private void subtaskPlanning( Problem problem, ArrayList<Rel> algorithm, boolean computeAll ) {
		
		if (isSubtaskLoggingOn()) 
			db.p( "!!!--------- Starting Planning With Subtasks ---------!!!" );
		
		int maxDepthBackup = maxDepth;
		
		if( !m_isSubtaskRepetitionAllowed ) {
			maxDepth = problem.getRelsWithSubtasks().size() - 1;
		}
		
		if (isSubtaskLoggingOn()) 
			db.p( "maxDepth: " + ( maxDepth + 1 ) );
		
		subtaskPlanningImpl( problem, algorithm, new LinkedHashSet<Rel>(), 0, computeAll );
		
		maxDepth = maxDepthBackup;
	}
	
	/**
	 * Goal-driven recursive (depth-first, exhaustive) search with backtracking
	 * 
	 * @param problem
	 * @param algorithm
	 * @param subtaskRelsInPath
	 * @param depth
	 * @param computeAll
	 */
	private void subtaskPlanningImpl(Problem problem, List<Rel> algorithm,
			Set<Rel> subtaskRelsInPath, int depth, boolean computeAll ) {

		Set<Rel> relsWithSubtasks = new LinkedHashSet<Rel>( problem.getRelsWithSubtasks() );
		
		//start building Maximal Linear Branch (MLB)
		MLB: while( !relsWithSubtasks.isEmpty() ) {
			
			if (isSubtaskLoggingOn()) {
				String print = p(depth) + "Starting new MLB with: ";
				for (Rel rel : relsWithSubtasks) {
					print += "\n" + p(depth) + "  " + rel.getParentObjectName() + " : " + rel.getDeclaration();
				}
				db.p( print );
			}
				
			
			Set<Var> newVars = new LinkedHashSet<Var>();

			// or children
			OR: for ( Iterator<Rel> subtaskRelIterator = relsWithSubtasks.iterator();
						subtaskRelIterator.hasNext(); ) {
				
				Rel subtaskRel = subtaskRelIterator.next();
				
				if (isSubtaskLoggingOn())
					db.p( p(depth) + "OR: depth: " + ( depth + 1 ) + " rel - " + subtaskRel.getParentObjectName() + " : " + subtaskRel.getDeclaration() );
				if( ( subtaskRel.getUnknownInputCount() > 0 ) 
						|| newVars.containsAll( subtaskRel.getOutputs() ) 
						|| problem.getFoundVars().containsAll( subtaskRel.getOutputs() ) ) {

					if (isSubtaskLoggingOn()) {
						db.p( p(depth) + "skipped" );
						if( subtaskRel.getUnknownInputCount() > 0 ) {
							db.p( p(depth) + "because unknown inputs count = " + subtaskRel.getUnknownInputCount() 
									+ " " + subtaskRel.printUnknownInputs() );
						} else if( newVars.containsAll( subtaskRel.getOutputs() ) ) {
							db.p( p(depth) + "because all outputs in newVars" );
						} else if( problem.getFoundVars().containsAll( subtaskRel.getOutputs() ) ) {
							db.p( p(depth) + "because all outputs in FoundVars" );
						}
					}
					continue OR;
				}

				Set<Rel> newPath = new LinkedHashSet<Rel>();

				if( !m_isSubtaskRepetitionAllowed && subtaskRelsInPath.contains(subtaskRel) ) {
					if (isSubtaskLoggingOn())
						db.p( p(depth) + "This rel with subtasks is already in use, path: " + newPath );
					continue;
				} else if( !m_isSubtaskRepetitionAllowed ) {
					if (isSubtaskLoggingOn())
						db.p( p(depth) + "This rel with subtasks can be used, path: " + newPath );
					newPath.addAll( subtaskRelsInPath );
					newPath.add( subtaskRel );
				}

				// this is true if all subtasks are solvable
				boolean allSolved = true;
				// and children
				AND: for (SubtaskRel subtask : subtaskRel.getSubtasks()) {
					if (isSubtaskLoggingOn())
						db.p( p(depth) + "AND: subtask - " + subtask );

					if( subtask.isIndependent() ) {
						if (isSubtaskLoggingOn())
							db.p( "Independent!!!" );
						if( subtask.isSolvable() == null ) {
							if (isSubtaskLoggingOn())
								db.p( "Not solved yet" );
							//independent subtask is solved only once
							Problem context = subtask.getContext();
							ArrayList<Rel> alg = invokePlaning( context, false );
							boolean solved = context.getFoundVars().containsAll( context.getGoals() );
							if( solved ) {
								subtask.setSolvable( Boolean.TRUE );
								subtask.getAlgorithm().addAll( alg );
							} else {
								subtask.setSolvable( Boolean.FALSE );
							}
							allSolved &= solved;
						} else if( subtask.isSolvable() == Boolean.TRUE ) {
							if (isSubtaskLoggingOn())
								db.p( "Already solved" );
							allSolved &= true;
						} else {
							if (isSubtaskLoggingOn())
								db.p( "Not solvable" );
							allSolved &= false;
						}
						if (isSubtaskLoggingOn())
							db.p( "End of independent subtask " + subtask );
					} else {
						// lets clone the environment
						Problem problemNew = problem.getCopy();
						// we need fresh cloned subtask instance
						SubtaskRel subtaskNew = problemNew.getSubtask(subtask);

						prepareSubtask( problemNew, subtaskNew );

						Set<Var> goals = new LinkedHashSet<Var>();
						addVarsToSet( subtaskNew.getOutputs(), goals );

						boolean solved = 
							linearForwardSearch( problemNew, subtaskNew.getAlgorithm(), 
									// never optimize here
									goals, true );

						if( solved ) {
							if (isSubtaskLoggingOn())
								db.p( p(depth) + "SOLVED subtask: " + subtaskNew );
							subtask.getAlgorithm().addAll( subtaskNew.getAlgorithm() );
							allSolved &= solved;
							continue AND;
						} else if( !solved && ( depth == maxDepth ) ) {
							if (isSubtaskLoggingOn())
								db.p( p(depth) + "NOT SOLVED and cannot go any deeper, subtask: " + subtaskNew );
							continue OR;
						}

						if (isSubtaskLoggingOn())
							db.p( p(depth) + "Recursing deeper" );

						List<Rel> newAlg = new ArrayList<Rel>( subtaskNew.getAlgorithm() );

						subtaskPlanningImpl( problemNew, newAlg, newPath, depth + 1, false );

						if (isSubtaskLoggingOn())
							db.p( p(depth) + "Back to depth " + ( depth + 1 ) );

						solved = linearForwardSearch( problemNew, newAlg, 
								// always optimize here in order to get rid of unnecessary subtask instances
								// temporary true while optimization does not work correctly
								goals, true );

						if (isSubtaskLoggingOn())
							db.p( p(depth) + ( solved ? "" : "NOT" ) + " SOLVED subtask: " + subtaskNew );

						allSolved &= solved;

						// if at least one subtask is not solvable, try another branch
						if( !allSolved ) {
							continue OR;
						}
						// copy algorithm from cloned subtask to old
						subtask.getAlgorithm().addAll( newAlg );
					}

				}

				if( allSolved ) {
					algorithm.add( subtaskRel );

					addVarsToSet( subtaskRel.getOutputs(), newVars );
					
					problem.getKnownVars().addAll( newVars );
					problem.getFoundVars().addAll( newVars );
					
					subtaskRelIterator.remove();
					
					if (isSubtaskLoggingOn()) {
						db.p( p(depth) + "SOLVED ALL SUBTASKS for " + subtaskRel.getParentObjectName() + " : " + subtaskRel.getDeclaration() );
						db.p( p(depth) + "Updating the problem graph and continuing building new MLB" );
					}
					
					continue MLB;

				} else if( !m_isSubtaskRepetitionAllowed ) {
					if (isSubtaskLoggingOn())
						db.p( p(depth) + "NOT SOLVED ALL subtasks, removing from path " + subtaskRel.getParentObjectName() + " : " + subtaskRel.getDeclaration() );
					newPath.remove( subtaskRel );
				}
			}
			
			//exit loop because there are no more rels with subtasks to be applied
			//(i.e. no more rels can introduce new variables into the algorithm)
			if (isSubtaskLoggingOn()) {
				db.p( p(depth) + "No more MLB can be constructed" ); 
			}
			break;
		}
	}

	private void prepareSubtask(Problem problem, Rel subtask) {
		// [x->y] assume x is known
		Set<Var> flatVars = new LinkedHashSet<Var>();
		addVarsToSet( subtask.getInputs(), flatVars );
		problem.getKnownVars().addAll( flatVars );
		problem.getFoundVars().addAll( flatVars );
	}
	
	private static int maxDepth = 2;//0..2, i.e. 3
	private static boolean m_isSubtaskRepetitionAllowed = false;
	
	private String p( int depth ) {
		String s = "\t";
		for( int i = 0; i < depth; i++ ) {
			s += s;
		}
		return s;
	}
	
	private boolean linearLoggingOn = false;
	private boolean subtaskLoggingOn = false;
	
	public Component getCustomOptionComponent() {
		final JSpinner spinner = new JSpinner( new SpinnerNumberModel( maxDepth + 1, 1, 10, 1 ) );
		spinner.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				Integer value = (Integer)((JSpinner)e.getSource()).getValue();
				maxDepth = value - 1;
				
				if (RuntimeProperties.isLogDebugEnabled())
					db.p( "maxDepth " + ( maxDepth + 1 ) );
			}});
		final JPanel panel = new JPanel()
		{
			public void setEnabled( boolean b ) {
				super.setEnabled( b );
				spinner.setEnabled( b );
			}
		};
		panel.add( new JLabel("Max Depth: ") );
		panel.add( spinner );
		
		panel.setEnabled( m_isSubtaskRepetitionAllowed );
		
		final JCheckBox chbox = new JCheckBox( "Allow subtask recursive repetition", m_isSubtaskRepetitionAllowed );
		
		chbox.addChangeListener( new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				m_isSubtaskRepetitionAllowed = chbox.isSelected();
				panel.setEnabled( m_isSubtaskRepetitionAllowed );
				
				if (RuntimeProperties.isLogDebugEnabled())
					db.p( "m_isSubtaskRepetitionAllowed " + m_isSubtaskRepetitionAllowed );
			}} );
		
		JPanel container1 = new JPanel( new GridLayout( 2, 0 ) );
		container1.setBorder( BorderFactory.createTitledBorder( "Planning settings" ) );
		container1.add( chbox );
		container1.add( panel );
		
		JPanel container2 = new JPanel( new GridLayout( 2, 0 ) );
		container2.setBorder( BorderFactory.createTitledBorder( "Logging options" ) );
		
		final JCheckBox linear = new JCheckBox( "Detailed linear planning", isLinearLoggingOn() );
		linear.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setLinearLoggingOn(linear.isSelected());
			}
		} );
		container2.add( linear );
		final JCheckBox subtask = new JCheckBox( "Detailed subtask planning", isSubtaskLoggingOn() );
		subtask.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setSubtaskLoggingOn(subtask.isSelected());
			}
		} );
		container2.add( subtask );
		
		JPanel main = new JPanel();
		main.setLayout( new BoxLayout( main, BoxLayout.Y_AXIS ) );
		main.add( container1 );
		main.add( container2 );
		
    	return main;
    }

	/**
	 * @param subtaskLoggingOn the subtaskLoggingOn to set
	 */
	private void setSubtaskLoggingOn(boolean subtaskLoggingOn) {
		this.subtaskLoggingOn = subtaskLoggingOn;
	}

	/**
	 * @return the subtaskLoggingOn
	 */
	private boolean isSubtaskLoggingOn() {
		return subtaskLoggingOn || RuntimeProperties.isLogDebugEnabled();
	}

	/**
	 * @param linearLoggingOn the linearLoggingOn to set
	 */
	private void setLinearLoggingOn(boolean linearLoggingOn) {
		this.linearLoggingOn = linearLoggingOn;
	}

	/**
	 * @return the linearLoggingOn
	 */
	private boolean isLinearLoggingOn() {
		return linearLoggingOn || RuntimeProperties.isLogDebugEnabled();
	}  
}
