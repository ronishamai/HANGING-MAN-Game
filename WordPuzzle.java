package il.ac.tau.cs.sw1.ex4;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class WordPuzzle {
	public static final char HIDDEN_CHAR = '_';
	public static final int MAX_VOCABULARY_SIZE = 3000;

	/*
	 * @pre: template is legal for word
	 */
	public static char[] createPuzzleFromTemplate(String word, boolean[] template) { // Q - 1
		char[] puzzle = new char[template.length];
		for (int i = 0; i < template.length; i++) {
			if (template[i] == true) {
				puzzle[i] = HIDDEN_CHAR;
			} else {
				puzzle[i] = word.charAt(i);
			}
		}
		return puzzle;
	}

	public static boolean checkLegalTemplate(String word, boolean[] template) { // Q - 2

		// Check if lengths of word and template are the same
		if (word.length() != template.length) {
			return false;
		}

		// Check if template contains both hidden and not hidden chars
		boolean containsHidden = false;
		boolean containsNotHidden = false;
		for (int i = 0; i < template.length; i++) {
			if (template[i] == true) {
				containsHidden = true;
			} else {
				containsNotHidden = true;
			}
		}
		if (containsHidden == false || containsNotHidden == false) {
			return false;
		}

		// Check if an identical character in word gets matching values in template
		for (int i = 0; i < word.length() - 1; i++) {
			char c = word.charAt(i);
			int nextIndexC = word.indexOf(c, i + 1); // Next occurance of c in word

			if (nextIndexC == -1) {
				continue;
			}

			if (template[i] != template[nextIndexC]) {
				return false;
			}
		}

		return true;
	}

	private static int kChooseN(int n, int k) {
		if (k > n) {
			return 0;
		}
		if (k == 0 || k == n) {
			return 1;
		}
		// Pascal identity
		return kChooseN(n - 1, k - 1) + kChooseN(n - 1, k);
	}

	private static String padLeft(String str, int n) {
		String pad = "";
		for (int i = 0; i < (n - str.length()); i++) {
			pad += "0";
		}
		return pad + str;
	}

	public static boolean[][] getAllLegalTemplates(String word, int k) {
		int n = word.length();
		boolean[][] legalTemplates = new boolean[kChooseN(n, k)][n];
		int legalTemplatesIdx = 0;

		for (int i = 1; i < Math.pow(2, n); i++) {
			String templateStr = Integer.toBinaryString(i);
			String templateStrPadded = padLeft(templateStr, n);

			boolean[] template = new boolean[n];
			int templateK = 0;
			for (int j = 0; j < templateStrPadded.length(); j++) {
				if (templateStrPadded.charAt(j) == '1') {
					templateK += 1;
					template[j] = true;
				}
			}

			if (templateK != k) {
				continue;
			}

			if (checkLegalTemplate(word, template)) {
				legalTemplates[legalTemplatesIdx] = template;
				legalTemplatesIdx++;
			}
		}

		return Arrays.copyOf(legalTemplates, legalTemplatesIdx);
	}

	/*
	 * @pre: puzzle is a legal puzzle constructed from word, guess is in [a...z]
	 */
	public static int applyGuess(char guess, String word, char[] puzzle) { // Q - 4
		int changedLetters = 0;

		for (int i = 0; i < word.length(); i++) {
			if ((puzzle[i] == HIDDEN_CHAR) && (word.charAt(i) == guess)) {
				puzzle[i] = guess;
				changedLetters += 1;
			}
		}

		return changedLetters;
	}

	/*
	 * @pre: puzzle is a legal puzzle constructed from word
	 * 
	 * @pre: puzzle contains at least one hidden character.
	 * 
	 * @pre: there are at least 2 letters that don't appear in word, and the user
	 * didn't guess
	 */
	public static char[] getHint(String word, char[] puzzle, boolean[] already_guessed) {
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		Random random = new Random();
		random.setSeed(4);
		char[] copyPuzzle = Arrays.copyOf(puzzle, puzzle.length);

		char hint1 = ' ';
		char hint2 = ' ';
		while (hint1 == ' ' || hint2 == ' ') {
			int r = random.nextInt(26);
			if (already_guessed[r]) {
				continue;
			}

			char c = alphabet.charAt(r);
			if (WordPuzzle.applyGuess(c, word, copyPuzzle) > 0) {
				if (hint1 == ' ') {
					hint1 = c;
				}
			} else {
				if (hint2 == ' ') {
					hint2 = c;
				}
			}
		}

		if (hint1 > hint2) {
			return new char[] { hint2, hint1 };
		} else {
			return new char[] { hint1, hint2 };
		}
	}

	private static char[] selectTemplate(String word, Scanner inputScanner) {
		printSelectTemplate();
		int selectedTemplateChoice = 0;
		if (inputScanner.hasNext()) {
			selectedTemplateChoice = inputScanner.nextInt();
		}

		if (selectedTemplateChoice == 1) {
			printSelectNumberOfHiddenChars();
			int numOfHiddenChars = 0;
			if (inputScanner.hasNext()) {
				numOfHiddenChars = inputScanner.nextInt();
			}

			boolean[][] legalTemplates = WordPuzzle.getAllLegalTemplates(word, numOfHiddenChars);
			if (legalTemplates.length == 0) {
				printWrongTemplateParameters();
				return WordPuzzle.selectTemplate(word, inputScanner);
			}

			Random random = new Random();
			random.setSeed(4);

			boolean[] randomTemplate = legalTemplates[random.nextInt(legalTemplates.length)];
			return WordPuzzle.createPuzzleFromTemplate(word, randomTemplate);
		} else if (selectedTemplateChoice == 2) {
			printEnterPuzzleTemplate();

			String inputTemplateStr = "";
			if (inputScanner.hasNext()) {
				inputTemplateStr = inputScanner.next();
			}
			String[] inputTemplateChars = inputTemplateStr.split(",");

			boolean[] inputTemplate = new boolean[inputTemplateChars.length];
			for (int i = 0; i < inputTemplateChars.length; i++) {
				if (inputTemplateChars[i].equals("_")) {
					inputTemplate[i] = true;
				}
			}

			if (WordPuzzle.checkLegalTemplate(word, inputTemplate)) {
				return WordPuzzle.createPuzzleFromTemplate(word, inputTemplate);
			} else {
				printWrongTemplateParameters();
				return WordPuzzle.selectTemplate(word, inputScanner);
			}
		}

		return null;
	}

	public static char[] mainTemplateSettings(String word, Scanner inputScanner) { // Q - 6
		printSettingsMessage();

		return selectTemplate(word, inputScanner);
	}

	private static char readUserGuess(Scanner inputScanner) {
		String userGuess = "";
		if (inputScanner.hasNext()) {
			userGuess = inputScanner.next();
		}
		return userGuess.charAt(0);
	}

	private static void stageC(String word, char[] puzzle, Scanner inputScanner, int guessesLeft,
			boolean[] alreadyGuessed) {
		System.out.println(puzzle);
		printEnterYourGuessMessage();

		char userGuess = readUserGuess(inputScanner);

		if (userGuess == 'H') {
			char[] hints = WordPuzzle.getHint(word, puzzle, alreadyGuessed);
			printHint(hints);
			stageC(word, puzzle, inputScanner, guessesLeft, alreadyGuessed);
			return;
		}

		guessesLeft--;
		alreadyGuessed[(int) userGuess - 'a'] = true;

		if (applyGuess(userGuess, word, puzzle) > 0) { // Correct guess
			if (new String(puzzle).indexOf(HIDDEN_CHAR) == -1) {
				printWinMessage();
				return;
			}

			printCorrectGuess(guessesLeft);
		} else { // Incorrect guess
			printWrongGuess(guessesLeft);
		}

		if (guessesLeft > 0) {
			stageC(word, puzzle, inputScanner, guessesLeft, alreadyGuessed);
		} else {
			printGameOver();
		}
	}

	public static void mainGame(String word, char[] puzzle, Scanner inputScanner) { // Q - 7
		printGameStageMessage();

		int numOfHiddenChars = 0;
		for (int i = 0; i < puzzle.length; i++) {
			if (puzzle[i] == HIDDEN_CHAR) {
				numOfHiddenChars += 1;
			}
		}
		int numOfGuess = numOfHiddenChars + 3;
		boolean[] alreadyGuessed = new boolean[26];

		stageC(word, puzzle, inputScanner, numOfGuess, alreadyGuessed);
	}

	/*************************************************************/
	/********************* Don't change this ********************/
	/*************************************************************/

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			throw new Exception("You must specify one argument to this program");
		}
		String wordForPuzzle = args[0].toLowerCase();
		if (wordForPuzzle.length() > 10) {
			throw new Exception("The word should not contain more than 10 characters");
		}
		Scanner inputScanner = new Scanner(System.in);
		char[] puzzle = mainTemplateSettings(wordForPuzzle, inputScanner);
		mainGame(wordForPuzzle, puzzle, inputScanner);
		inputScanner.close();
	}

	public static void printSettingsMessage() {
		System.out.println("--- Settings stage ---");
	}

	public static void printEnterWord() {
		System.out.println("Enter word:");
	}

	public static void printSelectNumberOfHiddenChars() {
		System.out.println("Enter number of hidden characters:");
	}

	public static void printSelectTemplate() {
		System.out.println("Choose a (1) random or (2) manual template:");
	}

	public static void printWrongTemplateParameters() {
		System.out.println("Cannot generate puzzle, try again.");
	}

	public static void printEnterPuzzleTemplate() {
		System.out.println("Enter your puzzle template:");
	}

	public static void printPuzzle(char[] puzzle) {
		System.out.println(puzzle);
	}

	public static void printGameStageMessage() {
		System.out.println("--- Game stage ---");
	}

	public static void printEnterYourGuessMessage() {
		System.out.println("Enter your guess:");
	}

	public static void printHint(char[] hist) {
		System.out.println(String.format("Here's a hint for you: choose either %s or %s.", hist[0], hist[1]));

	}

	public static void printCorrectGuess(int attemptsNum) {
		System.out.println("Correct Guess, " + attemptsNum + " guesses left.");
	}

	public static void printWrongGuess(int attemptsNum) {
		System.out.println("Wrong Guess, " + attemptsNum + " guesses left.");
	}

	public static void printWinMessage() {
		System.out.println("Congratulations! You solved the puzzle!");
	}

	public static void printGameOver() {
		System.out.println("Game over!");
	}

}
