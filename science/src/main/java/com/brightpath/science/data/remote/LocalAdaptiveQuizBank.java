package com.brightpath.science.data.remote;

import com.brightpath.science.domain.model.EducationLevel;
import com.brightpath.science.domain.model.QuizChallenge;
import com.brightpath.science.domain.model.QuizDifficulty;
import com.brightpath.science.domain.model.QuizQuestion;
import com.brightpath.science.domain.model.QuizRequest;
import com.brightpath.science.domain.model.ScienceSubject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/** Level-specific, continuously shuffled fallback used when the remote AI is unavailable. */
public final class LocalAdaptiveQuizBank {
        /*
 * Responsibility: Generates the Science result handled by `generate` using the configured generator/service and request context.
 * Purpose: Keeps content generation behind a focused method so the presentation layer stays independent from AI/fallback details.
 */
public List<QuizQuestion> generate(QuizRequest request) {
        List<QuizQuestion> bank = bankFor(request.getSubject(), request.getLevel(), request.getVariationSeed());
        Set<String> excluded = new HashSet<>();
        for (String prompt : request.getRecentlyUsedPrompts()) excluded.add(normalize(prompt));

        List<QuizQuestion> preferred = new ArrayList<>();
        List<QuizQuestion> repeated = new ArrayList<>();
        for (QuizQuestion question : bank) {
            if (excluded.contains(normalize(question.getPrompt()))) repeated.add(question);
            else preferred.add(question);
        }

        Random random = new Random(request.getVariationSeed());
        preferred = prioritizeForChallenge(preferred, request.getChallenge(), random);
        repeated = prioritizeForChallenge(repeated, request.getChallenge(), random);
        preferred.addAll(repeated);

        List<QuizQuestion> result = new ArrayList<>();
        for (QuizQuestion question : preferred) {
            result.add(shuffleOptions(applyDifficulty(question, request.getChallenge()), random).asLocal());
            if (result.size() >= request.getNumberOfQuestions()) break;
        }
        return result;
    }


    /**
     * Adapts the fallback question mix inside the already selected education level.
     * SUPPORT favours short, confidence-building items; CHALLENGE favours matching and
     * multi-step multiple-choice items. The level-specific bank itself is never changed.
     */
        /*
 * Responsibility: Performs the `prioritizeForChallenge` operation for LocalAdaptiveQuizBank within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
private List<QuizQuestion> prioritizeForChallenge(List<QuizQuestion> source,
                                                       QuizChallenge challenge,
                                                       Random random) {
        List<QuizQuestion> shuffled = new ArrayList<>(source);
        Collections.shuffle(shuffled, random);
        if (challenge == null || challenge == QuizChallenge.ON_LEVEL) {
            return shuffled;
        }

        List<QuizQuestion> ordered = new ArrayList<>(shuffled.size());
        QuizQuestion.Type[] priority = challenge == QuizChallenge.SUPPORT
                ? new QuizQuestion.Type[] {
                        QuizQuestion.Type.TRUE_FALSE,
                        QuizQuestion.Type.FILL_BLANK,
                        QuizQuestion.Type.MULTIPLE_CHOICE,
                        QuizQuestion.Type.MATCHING
                }
                : new QuizQuestion.Type[] {
                        QuizQuestion.Type.MATCHING,
                        QuizQuestion.Type.MULTIPLE_CHOICE,
                        QuizQuestion.Type.FILL_BLANK,
                        QuizQuestion.Type.TRUE_FALSE
                };

        for (QuizQuestion.Type type : priority) {
            for (QuizQuestion question : shuffled) {
                if (question.getType() == type) ordered.add(question);
            }
        }
        return ordered;
    }

        /*
 * Responsibility: Performs the `bankFor` operation for LocalAdaptiveQuizBank within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
private List<QuizQuestion> bankFor(ScienceSubject subject, EducationLevel level, long seed) {
        if (subject == ScienceSubject.CHEMISTRY) return chemistry(level, seed);
        if (subject == ScienceSubject.BIOLOGY) return biology(level, seed);
        return physics(level, seed);
    }

        /*
 * Responsibility: Performs the `physics` operation for LocalAdaptiveQuizBank within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
private List<QuizQuestion> physics(EducationLevel level, long seed) {
        int a = 3 + (int) Math.floorMod(seed, 5);
        int b = 2 + (int) Math.floorMod(seed / 7, 4);
        if (level == EducationLevel.UNIVERSITY) {
            return Arrays.asList(
                    mc("A particle moves with x(t)=3t²+2t. What is v(t)?",
                            "6t+2", "3t+2", "6t", "3t²", "6t+2",
                            "Velocity is the time derivative of position."),
                    tf("In an isolated system, total linear momentum is conserved.", true,
                            "Internal forces cancel in pairs, so total momentum stays constant."),
                    fill("For small oscillations, the angular frequency of a mass-spring system is ____.",
                            "sqrt(k/m)", "The simple harmonic oscillator has ω=√(k/m)."),
                    mc("Which statement best describes electric potential?",
                            "Potential energy per unit charge", "Force per unit charge", "Charge per unit area", "Current per unit resistance",
                            "Potential energy per unit charge", "Electric potential is measured in joules per coulomb."),
                    match("Match the law with its mathematical form.",
                            Arrays.asList("A. Gauss's law", "B. Faraday's law", "1. ∮E·dl = -dΦB/dt", "2. ∮E·dA = Qenc/ε0"),
                            "A-2, B-1", "Gauss connects electric flux to enclosed charge; Faraday connects induced electric field to changing magnetic flux."),
                    mc("A " + a + " kg object accelerates at " + b + " m/s². What is the net force?",
                            (a*b)+" N", (a+b)+" N", (a*b*b)+" N", (a+2*b)+" N", (a*b)+" N",
                            "Newton's second law gives F=ma."),
                    tf("The work done by a conservative force depends on the path taken.", false,
                            "For a conservative force, work depends only on the initial and final positions.")
            );
        }
        if (level == EducationLevel.HIGH_SCHOOL) {
            return Arrays.asList(
                    mc("A car travels " + (a*10) + " m in " + b + " s. What is its average speed?",
                            format((a*10.0)/b)+" m/s", (a*10*b)+" m/s", format(b/(a*10.0))+" m/s", (a*10-b)+" m/s",
                            format((a*10.0)/b)+" m/s", "Average speed equals distance divided by time."),
                    tf("The acceleration of an object can be non-zero while its speed is momentarily zero.", true,
                            "At the highest point of vertical motion, speed is zero but gravitational acceleration remains."),
                    fill("The kinetic energy of an object is calculated by ____.",
                            "1/2 mv^2", "Kinetic energy is one half mass times speed squared."),
                    mc("Which change increases the resistance of a uniform wire?",
                            "Increasing its length", "Increasing its cross-sectional area", "Using a lower-resistivity material", "Cooling every metal in all cases",
                            "Increasing its length", "For a uniform wire, R=ρL/A."),
                    match("Match the quantity with its SI unit.",
                            Arrays.asList("A. Power", "B. Electric charge", "1. Coulomb", "2. Watt"),
                            "A-2, B-1", "Power is measured in watts and charge in coulombs."),
                    mc("A force of " + a + " N moves an object " + b + " m in the force direction. Work done is:",
                            (a*b)+" J", (a+b)+" J", format(a/(double)b)+" J", (a*b)+" W", (a*b)+" J",
                            "For a constant parallel force, W=Fs."),
                    tf("In a parallel circuit, every branch has the same potential difference.", true,
                            "All branches connect across the same two nodes.")
            );
        }
        return Arrays.asList(
                mc("Which unit is used to measure force?", "Newton", "Joule", "Watt", "Volt", "Newton",
                        "Force is measured in newtons (N)."),
                tf("A series circuit has only one path for electric current.", true,
                        "All components in a series circuit share a single path."),
                fill("Speed equals distance divided by ____.", "time",
                        "Speed tells how much distance is travelled per unit time."),
                mc("Which object has the greatest gravitational potential energy?",
                        "The same ball on the highest shelf", "The ball on the floor", "A resting ball under the shelf", "All always have zero energy",
                        "The same ball on the highest shelf", "For the same mass, greater height means greater gravitational potential energy."),
                match("Match the energy form with the example.",
                        Arrays.asList("A. Light energy", "B. Sound energy", "1. Ringing bell", "2. Sunlight"),
                        "A-2, B-1", "Sunlight carries light energy; a ringing bell produces sound energy."),
                mc("A cyclist travels " + (a*b) + " km in " + b + " h. Average speed is:",
                        a+" km/h", (a*b*b)+" km/h", b+" km/h", (a+b)+" km/h", a+" km/h",
                        "Average speed equals distance divided by time."),
                tf("Energy can change from one form to another.", true,
                        "For example, a lamp changes electrical energy into light and heat.")
        );
    }

        /*
 * Responsibility: Performs the `chemistry` operation for LocalAdaptiveQuizBank within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
private List<QuizQuestion> chemistry(EducationLevel level, long seed) {
        int n = 2 + (int) Math.floorMod(seed, 4);
        if (level == EducationLevel.UNIVERSITY) {
            return Arrays.asList(
                    mc("Which thermodynamic quantity is minimized at constant temperature and pressure at equilibrium?",
                            "Gibbs free energy", "Enthalpy only", "Entropy only", "Internal energy in every process",
                            "Gibbs free energy", "At constant T and P, equilibrium corresponds to a minimum in Gibbs free energy."),
                    tf("A catalyst changes the equilibrium constant of a reaction.", false,
                            "A catalyst changes the rate of reaching equilibrium, not the equilibrium constant."),
                    fill("For a first-order reaction, the integrated rate law is ____.",
                            "ln[A]t = ln[A]0 - kt", "The logarithm of concentration decreases linearly with time."),
                    mc("Which orbital has one radial node for a hydrogen-like atom?",
                            "2s", "1s", "2p", "3d", "2s",
                            "The number of radial nodes is n-l-1; for 2s it is one."),
                    match("Match the technique with the information it commonly provides.",
                            Arrays.asList("A. IR spectroscopy", "B. NMR spectroscopy", "1. Chemical environments of nuclei", "2. Bond vibration frequencies"),
                            "A-2, B-1", "IR probes vibrational modes; NMR probes nuclear environments."),
                    mc("For an ideal gas, doubling absolute temperature at constant volume changes pressure by:",
                            "A factor of 2", "A factor of 4", "A factor of 1/2", "No change", "A factor of 2",
                            "From PV=nRT, pressure is proportional to absolute temperature at fixed volume."),
                    tf("A strong acid must always be highly concentrated.", false,
                            "Strength describes ionization; concentration describes amount per volume.")
            );
        }
        if (level == EducationLevel.HIGH_SCHOOL) {
            return Arrays.asList(
                    mc("Which equation is correctly balanced for forming water?",
                            "2H2 + O2 → 2H2O", "H2 + O2 → H2O", "H2 + 2O2 → H2O", "2H2 + 2O2 → H2O",
                            "2H2 + O2 → 2H2O", "Both sides contain four H atoms and two O atoms."),
                    tf("At 25°C, a solution with pH 3 is more acidic than a solution with pH 5.", true,
                            "Lower pH means a higher hydrogen-ion concentration."),
                    fill("The number of particles in one mole is approximately ____.",
                            "6.02 × 10^23", "This is Avogadro's constant."),
                    mc("Which bond is most likely formed between sodium and chlorine?",
                            "Ionic bond", "Metallic bond", "Non-polar covalent bond", "Hydrogen bond", "Ionic bond",
                            "Sodium transfers an electron to chlorine, producing oppositely charged ions."),
                    match("Match the gas with its common test.",
                            Arrays.asList("A. Hydrogen", "B. Carbon dioxide", "1. Turns limewater milky", "2. Produces a squeaky pop"),
                            "A-2, B-1", "Hydrogen gives a squeaky pop; carbon dioxide turns limewater milky."),
                    mc("How many moles are in " + (n*18) + " g of water (Mr=18)?",
                            n+" mol", (n*18)+" mol", format(18.0/n)+" mol", "18 mol", n+" mol",
                            "Amount in moles equals mass divided by molar mass."),
                    tf("Oxidation can be described as loss of electrons.", true,
                            "OIL RIG: Oxidation Is Loss, Reduction Is Gain of electrons.")
            );
        }
        return Arrays.asList(
                mc("What is the chemical formula for water?", "H2O", "CO2", "O2", "NaCl", "H2O",
                        "A water molecule has two hydrogen atoms and one oxygen atom."),
                tf("A physical change always creates a new substance.", false,
                        "A physical change changes form or state without creating a new substance."),
                fill("The smallest unit of an element that keeps its properties is an ____.", "atom",
                        "Atoms are the basic units of elements."),
                mc("Which of these is an element?", "Oxygen", "Water", "Salt water", "Carbon dioxide", "Oxygen",
                        "Oxygen contains only one type of atom."),
                match("Match the state with particle behaviour.",
                        Arrays.asList("A. Solid", "B. Gas", "1. Particles spread far apart", "2. Particles vibrate in fixed positions"),
                        "A-2, B-1", "Solid particles remain close and vibrate; gas particles move freely far apart."),
                mc("Which sign suggests that a chemical reaction may have happened?",
                        "Gas bubbles form", "An object is cut smaller", "Ice melts", "Water is poured", "Gas bubbles form",
                        "Unexpected gas production can indicate a new substance has formed."),
                tf("The symbol Na represents sodium.", true,
                        "Na comes from the Latin name natrium.")
        );
    }

        /*
 * Responsibility: Performs the `biology` operation for LocalAdaptiveQuizBank within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
private List<QuizQuestion> biology(EducationLevel level, long seed) {
        int n = 2 + (int) Math.floorMod(seed, 3);
        if (level == EducationLevel.UNIVERSITY) {
            return Arrays.asList(
                    mc("Which enzyme synthesizes RNA from a DNA template during transcription?",
                            "RNA polymerase", "DNA ligase", "Ribosome", "Reverse transcriptase in all cells",
                            "RNA polymerase", "RNA polymerase reads DNA and builds a complementary RNA strand."),
                    tf("Competitive inhibition can often be reduced by increasing substrate concentration.", true,
                            "More substrate can outcompete a competitive inhibitor for the active site."),
                    fill("The membrane potential at which net ion movement is zero for one ion is its ____ potential.",
                            "equilibrium", "The Nernst potential is the ion's electrochemical equilibrium potential."),
                    mc("Which process most directly increases genetic variation during meiosis?",
                            "Crossing over", "DNA replication alone", "Cytokinesis alone", "Binary fission", "Crossing over",
                            "Crossing over exchanges DNA between homologous chromosomes."),
                    match("Match the method with its main use.",
                            Arrays.asList("A. PCR", "B. Western blot", "1. Detect a specific protein", "2. Amplify a DNA sequence"),
                            "A-2, B-1", "PCR amplifies DNA; Western blotting detects proteins."),
                    mc("In Hardy-Weinberg equilibrium, if q=" + format(1.0/n) + ", the recessive genotype frequency is:",
                            format(1.0/(n*n)), format(2.0/n), format(1.0/n), format(1.0-1.0/n), format(1.0/(n*n)),
                            "The recessive homozygous genotype frequency is q²."),
                    tf("Allosteric enzymes can show cooperative substrate binding.", true,
                            "Interactions between subunits can produce sigmoidal kinetics.")
            );
        }
        if (level == EducationLevel.HIGH_SCHOOL) {
            return Arrays.asList(
                    mc("Which organelle is the main site of aerobic respiration?",
                            "Mitochondrion", "Nucleus", "Golgi apparatus", "Cell wall", "Mitochondrion",
                            "Most ATP from aerobic respiration is produced in mitochondria."),
                    tf("DNA replication is described as semi-conservative.", true,
                            "Each new DNA molecule contains one original strand and one newly synthesized strand."),
                    fill("The process by which plants lose water vapour through leaves is ____.",
                            "transpiration", "Water vapour mainly exits through stomata."),
                    mc("Two heterozygous parents Aa × Aa have what probability of an aa child?",
                            "25%", "50%", "75%", "100%", "25%",
                            "The genotype ratio is 1 AA : 2 Aa : 1 aa."),
                    match("Match the blood component with its function.",
                            Arrays.asList("A. Red blood cell", "B. Platelet", "1. Blood clotting", "2. Oxygen transport"),
                            "A-2, B-1", "Red cells carry oxygen; platelets help clotting."),
                    mc("A population grows from 100 to " + (100*n) + ". By what factor did it grow?",
                            String.valueOf(n), String.valueOf(n+1), String.valueOf(100*n), format(1.0/n), String.valueOf(n),
                            "Growth factor equals final population divided by initial population."),
                    tf("Natural selection acts on heritable variation within populations.", true,
                            "Individuals with advantageous heritable traits may leave more offspring.")
            );
        }
        return Arrays.asList(
                mc("What is the basic unit of life?", "Cell", "Organ", "Tissue", "Atom", "Cell",
                        "All living organisms are made of one or more cells."),
                tf("Plants usually release oxygen during photosynthesis.", true,
                        "Oxygen is produced as water is split during photosynthesis."),
                fill("The organ that pumps blood around the body is the ____.", "heart",
                        "The heart contracts to move blood through blood vessels."),
                mc("Which body system is mainly responsible for gas exchange?",
                        "Respiratory system", "Digestive system", "Skeletal system", "Urinary system", "Respiratory system",
                        "The lungs exchange oxygen and carbon dioxide."),
                match("Match the structure with its function.",
                        Arrays.asList("A. Root", "B. Leaf", "1. Main site of photosynthesis", "2. Absorbs water and minerals"),
                        "A-2, B-1", "Roots absorb water and minerals; leaves carry out most photosynthesis."),
                mc("Which characteristic is shared by all living things?",
                        "They carry out life processes", "They all move from place to place", "They all make their own food", "They all have flowers",
                        "They carry out life processes", "Living things perform processes such as respiration, growth and reproduction."),
                tf("All bacteria are harmful to humans.", false,
                        "Many bacteria are harmless or beneficial, including those used in digestion and food production.")
        );
    }


        /*
 * Responsibility: Applies the transformation/style/rule represented by `applyDifficulty` to the current Science data or UI state.
 * Purpose: Encapsulates the change in one place so it can be reused consistently.
 */
private QuizQuestion applyDifficulty(QuizQuestion question, QuizChallenge challenge) {
        QuizDifficulty difficulty = question.getDifficulty();
        if (challenge == QuizChallenge.SUPPORT) {
            difficulty = question.getType() == QuizQuestion.Type.MATCHING
                    ? QuizDifficulty.MEDIUM : QuizDifficulty.EASY;
        } else if (challenge == QuizChallenge.CHALLENGE) {
            difficulty = question.getType() == QuizQuestion.Type.TRUE_FALSE
                    ? QuizDifficulty.MEDIUM : QuizDifficulty.HARD;
        }
        return new QuizQuestion(
                question.getType(),
                question.getPrompt(),
                question.getOptions(),
                question.getCorrectAnswer(),
                question.getExplanation(),
                difficulty
        );
    }

        /*
 * Responsibility: Performs the `shuffleOptions` operation for LocalAdaptiveQuizBank within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
private QuizQuestion shuffleOptions(QuizQuestion question, Random random) {
        if (question.getType() != QuizQuestion.Type.MULTIPLE_CHOICE
                && question.getType() != QuizQuestion.Type.TRUE_FALSE) return question;
        List<String> options = new ArrayList<>(question.getOptions());
        Collections.shuffle(options, random);
        return new QuizQuestion(question.getType(), question.getPrompt(), options,
                question.getCorrectAnswer(), question.getExplanation(), question.getDifficulty());
    }

        /*
 * Responsibility: Performs the `mc` operation for LocalAdaptiveQuizBank within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
private QuizQuestion mc(String prompt, String a, String b, String c, String d,
                            String answer, String explanation) {
        return new QuizQuestion(QuizQuestion.Type.MULTIPLE_CHOICE, prompt,
                Arrays.asList(a, b, c, d), answer, explanation);
    }

        /*
 * Responsibility: Performs the `tf` operation for LocalAdaptiveQuizBank within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
private QuizQuestion tf(String prompt, boolean answer, String explanation) {
        return new QuizQuestion(QuizQuestion.Type.TRUE_FALSE, prompt,
                Arrays.asList("True", "False"), answer ? "True" : "False", explanation);
    }

        /*
 * Responsibility: Performs the `fill` operation for LocalAdaptiveQuizBank within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
private QuizQuestion fill(String prompt, String answer, String explanation) {
        return new QuizQuestion(QuizQuestion.Type.FILL_BLANK, prompt,
                Collections.emptyList(), answer, explanation);
    }

        /*
 * Responsibility: Performs the `match` operation for LocalAdaptiveQuizBank within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
private QuizQuestion match(String prompt, List<String> options,
                               String answer, String explanation) {
        return new QuizQuestion(QuizQuestion.Type.MATCHING, prompt, options, answer, explanation);
    }

        /*
 * Responsibility: Normalizes or formats the value handled by `normalize` into a stable representation.
 * Purpose: Improves consistency for display, comparison, caching, storage, or AI prompt construction.
 */
private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

        /*
 * Responsibility: Normalizes or formats the value handled by `format` into a stable representation.
 * Purpose: Improves consistency for display, comparison, caching, storage, or AI prompt construction.
 */
private String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.00001) {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
