package watermixpuzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WaterMixPuzzle {

	// Light green = 0
	// Grey        = 1
	// Light blue  = 2
	// Orange      = 3
	// Pink        = 4
	// Yellow      = 5
	// Red         = 6
	// Dark Green  = 7
	// Purple      = 8
	// Dark Blue   = 9
	// Brown       = A
	// Swamp Green = B

	// Level 141
	public static String[] puzzle = {
			"0123",
			"3345",
			"5367",
			"8892",
			"57A9",
			"694B",
			"2A9A",
			"B412",
			"4B68",
			"50B6",
			"11A0",
			"0877",
			"    ",
			"    "
	};

	//	public static String[] puzzle = {"    ","AAAB", "ABBB"};

	public static int depth = 4;
	public static List<String[]> solutionTubes = new ArrayList<>();

	public static String emptyTube = new String(new char[depth]).replace('\0', ' ');

	public static void main(String[] args) {
		// If a puzzle is sent on the command line, we use that instead of one hard coded here.
		if (args == null) {
			puzzle = args;
		}

		// Check that the puzzle supplied is valid (must have at least 2 tubes and all tubes must
		// contain the number of items specified
		// by the depth int.
		if (puzzle.length < 2) {
			System.out.println("!!!NOT A VALID PUZZLE!!!");
			System.out.println("2 tubes at least need to be supplied, but "
					+ puzzle.length
					+ " have been supplied.");
			System.exit(1);
		}
		String invalidTube = validPuzzle(puzzle);
		if (invalidTube != null) {
			System.out.println("!!!NOT A VALID PUZZLE!!!");
			System.out.println("All 'tubes' need to contain " + depth + " elements.");
			System.out.println("This tube does not (there may be other invalid tubes)."
					+ System.lineSeparator());
			System.out.println(display(new String[]{invalidTube}, invalidTube.length()));
			System.exit(1);
		}

		// Try and solve the puzzle by back tracking recursion.
		if (solve(puzzle)) {
			// Puzzle was solved, output each step in the solution.
			Collections.reverse(solutionTubes);
			System.out.println(display(puzzle));
			System.out.println("Solution found:" + System.lineSeparator());
			int stepNumber = 0;
			for (String[] step : solutionTubes) {
				stepNumber++;
				System.out.println(display(getTubesFrom(step)));
				System.out.println("Step " + stepNumber + ". " + step[step.length - 1]);
			}
		} else {
			// No solution was found.
			System.out.println(display(puzzle));
			System.out.println("!!!NO SOLUTION FOUND!!!" + System.lineSeparator());
		}
	}

	private static String[] getTubesFrom(String[] step) {
		String[] tubes = new String[step.length - 1];

		if (tubes.length >= 0) {
			System.arraycopy(step, 0, tubes, 0, tubes.length);
		}

		return tubes;
	}

	private static boolean solve(String[] currentTubes) {
		if (isSolved(currentTubes)) {
			addSolutionStep(currentTubes, "Done");
			return true;
		}

		for (int fromTube = 0; fromTube < currentTubes.length; fromTube++) {
			for (int toTube = 0; toTube < currentTubes.length; toTube++) {
				// Can't move a tube to itself.
				if (toTube == fromTube) {
					continue;
				}

				String[] newTubes = currentTubes.clone();
				if (moveIfValid(newTubes, fromTube, toTube)) {
					if (solve(newTubes)) {
						addSolutionStep(currentTubes,
								"Move from " + (fromTube + 1) + " to " + (toTube + 1));
						return true;
					}
					// if we this valid move did ultimately solve the puzzle, we continue looking
					// for other valid moves.
				}
			}
		}

		// We have tried every combination of moving liquids from one tube to another, either
		// there are no valid moves from this
		// position, or all valid moves ultimately led to no solution.
		return false;
	}

	private static void addSolutionStep(String[] currentTubes, String message) {
		int length = currentTubes.length + 1;
		String[] step = new String[length];

		System.arraycopy(currentTubes, 0, step, 0, currentTubes.length);

		step[length - 1] = message;

		solutionTubes.add(step);
	}

	private static boolean moveIfValid(String[] tubes, int from, int to) {
		// Attempt to move the liquid from one tube to another.
		// This is only allowed if
		// a) Not allowed to entire contents of a tube to an empty tube (this could create
		// infinite loops with liquids being bounced backwards and forwards.)
		// b) Not allowed to move more liquid than will fit into the target tube
		// c) Can only move a liquid to an empty tube or on-top of a liquid of the same colour.
		// d) Must move as much liquid as there is of one colour from the from tube.
		// e) Not allowed to move the 'contents' from an empty tube.
		// Example allowed move:
		// " AABA" to "   AB" -> "    B" "AAABA" (2 A's are moved on-top of the A in the other
		// tube - NOTE we must move both A's).
		// "Example invalid moves:
		// " CCCC" "     " (invalid by a).
		// " CCCC" " CAAA" (invalid by b and d - Must attempt to move all 4 C's and these won't
		// fit as there is only 1 available space, we are not allowed to  move just one).
		// " CCCC" " ACCC" (invalid by c) ).
		// "     " "ABCDE" (invalid by e - can't move from an empty tube, even though it would
		// technically fit as 5 + 0 = 5).
		String fromTube = tubes[from];
		String toTube = tubes[to];

		// Rule e
		if (fromTube.equals(emptyTube)) {
			return false;
		}

		// Rule a
		if (tubeContainsOnlyOneColour(fromTube) && toTube.equals(emptyTube)) {
			return false;
		}

		// Rule c
		if (!toTube.equals(emptyTube) && !topColoursSame(fromTube, toTube)) {
			return false;
		}

		String colourToMove = getAllTopColour(fromTube);

		String newToTube = padLeft(colourToMove + toTube.trim(), depth);

		// Rule b
		if (newToTube.length() > depth) {
			return false;
		}

		String newFromTube = padLeft(fromTube.trim().substring(colourToMove.length()), depth);

		tubes[from] = newFromTube;
		tubes[to] = newToTube;

		return true;
	}

	private static boolean isSolved(String[] tubes) {
		// Each tube must be fully empty or full of the same letter, if all tubes meet this
		// criteria, then the puzzle is solved.
		for (String tube : tubes) {
			// Construct a string which represents what a tube full of the first digit found in
			// the tube would look like.
			if (!stringBasedOnFirstLetter(tube).equals(tube)) {
				return false;
			}
		}
		return true;
	}

	private static String getAllTopColour(String tube) {
		String tubeWithNoSpace = tube.trim();
		String colour = "";
		String firstColour = tubeWithNoSpace.substring(0, 1);

		boolean sameColour = true;
		int pos = 0;

		while (pos < tubeWithNoSpace.length() && sameColour) {
			String cell = tubeWithNoSpace.substring(pos, pos + 1);
			if (cell.equals(firstColour)) {
				colour += cell;
			} else {
				sameColour = false;
			}
			pos++;
		}
		return colour;
	}

	private static boolean topColoursSame(String fromTube, String toTube) {
		String fromTubeWithNoSpace = fromTube.trim();
		String toTubeWithNoSpace = toTube.trim();

		return fromTubeWithNoSpace.substring(0, 1).equals(toTubeWithNoSpace.substring(0, 1));
	}

	private static boolean tubeContainsOnlyOneColour(String tube) {
		String tubeWithNoSpace = tube.trim();
		return tubeWithNoSpace.equals(stringBasedOnFirstLetter(tubeWithNoSpace));
	}

	private static String stringBasedOnFirstLetter(String tube) {
		// If "ABC" is supplied, then "AAA" is returned.
		// If "ABCD" is supplied, then "AAAA" is returned.
		return new String(new char[tube.length()]).replace('\0',
				tube.substring(0, 1).toCharArray()[0]);
	}

	public static String padLeft(String s, int n) {
		while (s.length() < n) {
			s = " " + s;
		}

		return s;
	}

	private static String display(String[] tubes) {
		return display(tubes, depth);
	}

	private static String display(String[] tubes, int depth) {
		// Output the supplied array of tubes in a readable format.
		StringBuilder visualisation = new StringBuilder("");
		String spacer = "   ";
		String side = "|";

		for (int d = 1; d <= depth; d++) {
			visualisation.append(System.lineSeparator());
			visualisation.append(spacer);

			for (String tube : tubes) {
				visualisation.append(side);
				visualisation.append(tube.charAt(d - 1));
				visualisation.append(side);
				visualisation.append(spacer);
			}
			visualisation.append(spacer);
		}

		return visualisation.toString();
	}

	private static String validPuzzle(String[] tubes) {
		for (String tube : tubes) {
			if (tube.length() != depth) {
				return tube;
			}
		}

		return null;
	}
}
