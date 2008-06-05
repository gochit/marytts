/**
 * Copyright 2003-2007 DFKI GmbH.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * Permission is hereby granted, free of charge, to use and distribute
 * this software and its documentation without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of this work, and to
 * permit persons to whom this work is furnished to do so, subject to
 * the following conditions:
 * 
 * 1. The code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 * 2. Any modifications must be clearly marked as such.
 * 3. Original authors' names are not deleted.
 * 4. The authors' names are not used to endorse or promote products
 *    derived from this software without specific prior written
 *    permission.
 *
 * DFKI GMBH AND THE CONTRIBUTORS TO THIS WORK DISCLAIM ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS, IN NO EVENT SHALL DFKI GMBH NOR THE
 * CONTRIBUTORS BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 * PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS
 * ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */

package de.dfki.lt.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.dfki.lt.mary.modules.phonemiser.Phoneme;
import de.dfki.lt.mary.modules.phonemiser.PhonemeSet;

/**
 * 
 * This trains an alignment model between Strings. Applications are for example 
 * letter-to-sound rule training (see LTSTrainer) or transducer 
 * construction/minimization.
 * 
 * @author benjaminroth
 *
 */
public class AlignerTrainer {
    
    private Map<String, Integer> aligncost;

    private int maxCost = 10; // maybe we only need maxCost
    private int defaultcost = maxCost;
    private int skipcost;

    private double logOf2 = Math.log(2.0);

    
    protected String[] graphemeStrings;
    protected String[] phoneStrings;

    private PhonemeSet phonemeSet;
    protected Set<String> graphemeSet;
    
    
    private Locale locale;
    
    /**
     * This initializes the letter-to-sound trainer with the phoneme set
     *  that is mainly used for splitting phoneme chains, and a Locale
     *  that tells eg. how to lowercase words.
     */
    public AlignerTrainer(PhonemeSet aPhSet, Locale aLocale){
        this.skipcost = this.defaultcost;
        this.aligncost = new HashMap<String, Integer>();

        this.phonemeSet = aPhSet;
        this.locale = aLocale;
    }
    
    /**
     * 
     * This reads in a lexicon in "mary" format, lines are of the kind:
     * graphemechain\phonemechain\otherinformation
     * 
     * stress is preserved (TODO: make stress handling optional)
     * 
     * @param lexicon reader with the lines of the lexicon
     * @throws IOException
     */
    public void readLexicon(BufferedReader lexicon) throws IOException{
        
        ArrayList<String> graphemeStringList = new ArrayList<String>();
        ArrayList<String> phoneStringList = new ArrayList<String>();
        
        // store set of graphemes
        this.graphemeSet = new HashSet<String>();
        
        String line;
        
        while ((line = lexicon.readLine()) != null){
            String[] lineParts = line.trim().split("\\\\");

            String graphStr = lineParts[0].toLowerCase(this.locale);
            
            // remove all secondary stress markers
            String phonStr = lineParts[1].replaceAll(",", "");
            
            String[] syllables = phonStr.split("-");
            
            
            String seperatedPhones = "";
            String seperatedGraphemes = "";
            
            for (String syl : syllables){
            
                boolean stress = false;
                
                if (syl.startsWith("'")){
                    syl = syl.substring(1);
                    stress = true;
                }
                
                for ( Phoneme ph : phonemeSet.splitIntoPhonemes(syl)){
                    seperatedPhones += ph.name();
                    
                    if (stress && ph.isVowel()){
                        
                        seperatedPhones += "1";
                        stress = false;
                    }
                    
                    seperatedPhones += " ";
                }// ... for each phoneme
                
            }
            
            for ( int i = 0 ; i < graphStr.length() ; i++ ){
                
                this.graphemeSet.add(graphStr.substring(i, i+1));
                
                seperatedGraphemes += graphStr.substring(i, i+1) + " ";
            }
            
            graphemeStringList.add(seperatedGraphemes);
            phoneStringList.add(seperatedPhones);
            
        }
        
        this.graphemeStrings = graphemeStringList.toArray(new String[]{});
        this.phoneStrings    = phoneStringList.toArray(new String[]{});       
    }
    
    /**
     * 
     * reads in a lexicon in "sampa" format, lines are of the kind:
     * 
     * graphemechain\phonemechain\otherinformation
     * 
     * Stress is optionally preserved, marking the firsr vowel of a stressed
     * syllable with "1".
     * 
     * @param lexicon reader with lines of lexicon
     * @param considerStress indicator if stress is preserved
     * @throws IOException
     */
    public void readSampaLexicon(BufferedReader lexicon, boolean considerStress) throws IOException{
        
        ArrayList<String> graphemeStringList = new ArrayList<String>();
        ArrayList<String> phoneStringList = new ArrayList<String>();
        
        // store set of graphemes
        this.graphemeSet = new HashSet<String>();
        
        String line;
        
        while ((line = lexicon.readLine()) != null){
            String[] lineParts = line.trim().split(" ");
            // TODO: remove all non-standard symbols from input side, not only ' and -
            String graphStr = lineParts[0].toLowerCase(this.locale).replaceAll("['-.]", "");
            
            // remove all secondary stress markers
            String phonStr = lineParts[1].replaceAll(",", "");
            
            String[] syllables = phonStr.split("-");
            
            
            String separatedPhones = "";
            String separatedGraphemes = "";
            
            for (String syl : syllables){
            
                boolean stress = false;
                
                if (syl.startsWith("'")){
                    syl = syl.substring(1);
                    stress = true;
                }
                
                for ( Phoneme ph : phonemeSet.splitIntoPhonemes(syl)){
                    separatedPhones += ph.name();
                    
                    if (stress && considerStress && ph.isVowel()){
                        
                        separatedPhones += "1";
                        stress = false;
                    }
                    
                    separatedPhones += " ";
                }// ... for each phoneme
                
            }

            
            
            
            for ( int i = 0 ; i < graphStr.length() ; i++ ){
                
                this.graphemeSet.add(graphStr.substring(i, i+1));
                
                separatedGraphemes += graphStr.substring(i, i+1) + " ";
            }
            
            graphemeStringList.add(separatedGraphemes);
            phoneStringList.add(separatedPhones);
            
        }
        
        this.graphemeStrings = graphemeStringList.toArray(new String[]{});
        this.phoneStrings    = phoneStringList.toArray(new String[]{});       
    }
    
    /**
     * One iteration of alignment, using adapted Levenshtein distance. 
     * After the iteration, the costs between a grapheme and a phoneme are
     * set by the log probability of the phoneme given the grapheme. Analogously,
     * The deletion cost is set by the log of deletion probability.
     * In the first iteration, all operations cost maxCost.
     * 
     */
    public void alignIteration(){
        
        // this counts how many times a symbol is mapped to symbols
        Map<String, Integer> symMapCount = new HashMap<String, Integer>();
        
        // this counts how often particular mappings from one symbol to another occurred
        Map<String, Integer> sym2symCount = new HashMap<String, Integer>();
        
        // how many symbols are on input side
        int symCount = 0;
        
        // how many symbols are deleted
        int symDels = 0;
        
        // for every alignment pair collect counts
        for ( int i = 0; i < this.phoneStrings.length; i++ ){
            
            String alignment = this.distanceAlign(this.graphemeStrings[i], this.phoneStrings[i]);
            
            //System.out.println("---");
            //System.out.println(this.graphemeStrings[i]);
            //System.out.println(alignment);
            
            String[] in = this.graphemeStrings[i].trim().split(" ");
            // assure that there is at least one space sign after last '#'
            String[] out = alignment.concat(" ").split("#");
            
            assert(in.length == out.length);
            
            symCount += in.length;
            
            // for every input symbol...
            for ( int inNr = 0; inNr < in.length; inNr++){
                
                String outStr = out[inNr].trim();
                                
                if (outStr.length() == 0){
                    // is mapped to empty string
                    
                    symDels++;
                } else {
                    // mapped to one or several symbols
                    
                    String[] outs = outStr.split(" ");
                    
                    // increase count of overall mappings for this symbol
                    Integer c = symMapCount.get(in[inNr]);
                    if (null == c){                    
                        symMapCount.put(in[inNr], outs.length);
                    } else {
                        symMapCount.put(in[inNr], outs.length + c);
                    }
                    
                    // for every corresponding output symbol
                    for (int outNr = 0; outNr < outs.length; outNr ++){
                        
                        // get key for mapping symbol to symbol
                        String key = in[inNr].trim() + " " + outs[outNr].trim();
                        
                        Integer mapC = sym2symCount.get(key);
                        if (null == mapC){                    
                            sym2symCount.put(key, 1);
                        } else {
                            sym2symCount.put(key, 1 + mapC);
                        }
                    } // ...for each output-symbol
                } // ...if > 0 output-symbols    
            } // ...for each input symbol
        } // ...for each input string
        
        // now build fractions, to estimate the new costs
        
        // first reset skip costs
        double delFraction = (double) symDels / symCount ;
        this.skipcost = (int) -this.log2(delFraction);
        
        // now reset aligncosts
        this.aligncost.clear();
        
        for (String mapping : sym2symCount.keySet()){
            
            String firstSym = mapping.split(" ")[0];
            
            double fraction = (double) sym2symCount.get(mapping) / symMapCount.get(firstSym);
            int cost = (int) -this.log2( fraction );
            
            if ( cost < this.maxCost ) {
                this.aligncost.put(mapping, cost);
                //System.out.println(mapping + " -> " + cost);
            }
        }        
    }
    
    public int lexiconSize(){
        return this.graphemeStrings.length;
    }
    
    /**
     * 
     * gets an alignment of the graphemes to the phonemes of an entry.
     * a String array is returned, where every entry contains a grapheme 
     * together with the phoneme sequence it it mapped to, seperated by a
     * colon.
     * 
     * @param entryNr
     */
    public List<String>[] getAlignment(int entryNr){
        
        String align = this.distanceAlign(graphemeStrings[entryNr], phoneStrings[entryNr]);
        
        String[] in = graphemeStrings[entryNr].trim().split(" ");
        String[] out = align.concat(" ").split("#");
        
        // TODO: maybe do everything with lists
        ArrayList<String>[] listArray = new ArrayList[in.length];
                
        for (int pos = 0; pos < in.length ; pos++){
            
            ArrayList<String> alList = new ArrayList<String>(2);
            alList.add(in[pos].trim());            
            alList.add(out[pos].trim().replaceAll(" ", ""));
            
            listArray[pos] = alList;
            
        }
        
        return listArray;
    }
    
    private double log2(double d){
        return Math.log(d) / logOf2;
    }
    
    private int symDist(String aString1, String aString2) {
        
        String key = aString1 + " " + aString2;
        
        // if a value is stored, return it
        if (this.aligncost.containsKey(key)){
            return aligncost.get(key);
        } else {       
            // otherwise use 0 for equal symbols and defaultcost for different symbols
            return (aString1.equals(aString2))? 0:this.defaultcost;
        }
    }
    
    /**
     * 
     * This computes the alignment that has the lowest distance between two 
     * Strings.
     * 
     * There are three differences to the normal Levenshtein-distance:
     * 
     * 1. Only insertions and deletions are allowed, no replacements (i.e. no 
     *    "diagonal" transitions)
     * 2. insertion costs are dependent on a particular phone on the input side
     *    (the one they are aligned to)
     * 3. deletion is equivalent to a symbol on the input side that is not 
     *    aligned. There are costs associated with that.
     *    
     * The method returns the output string with alignment boundaries ('#') 
     * inserted.
     * 
     * @param in
     * @param out
     * @return
     */
    protected String distanceAlign(String in, String out ) {
        String[] istr = in.split(" ");
        String[] ostr = out.split(" ");
        String delim = "#";
        
        // distances:
        // 1. previous distance (= previous column in matrix)
        int[] p_d = new int[ostr.length+1];
        // 2. current distance
        int[] d = new int[ostr.length+1];
        // 3. dummy array for swapping, when switching to new column
        int[] _d;
        
        // array indicating if a skip was performed (= if current character has not been aligned)
        // same arrays as for distances
        boolean[] p_sk = new boolean[ ostr.length + 1 ];
        boolean[] sk   = new boolean[ ostr.length + 1 ];
        boolean[] _sk;
        
        // arrays storing the alignments corresponding to distances
        String[] p_al = new String[ ostr.length + 1 ];
        String[] al   = new String[ ostr.length + 1 ];
        String[] _al;
        
        // initialize values
        p_d[0]  = 0;
        p_al[0] = "";
        p_sk[0] = true;

        
        // ... still initializing
        for (int j = 1; j < ostr.length + 1; j++){
            // only possibility first is to align the first letter 
            // of the input string to everything
            p_al[j] = p_al[j-1] + " " + ostr[j-1]; 
            p_d[j] = p_d[j-1] + symDist(istr[0],ostr[j-1]);
            p_sk[j] = false;        
        }
        
        // constant penalty for not aligning a character
        int skConst = this.skipcost;
        
        // align
        // can start at 1, since 0 has been treated in initialization
        for (int i=1; i < istr.length; i++) {
            
            // zero'st row stands for skipping from the beginning on
            d[0] = p_d[0] + skConst ;//+ this.skipDist(istr[i-1]);
            al[0] = p_al[0] + " " + delim;
            sk[0] = true;
            
            for (int j = 1 ; j < ostr.length + 1; j++ ) {
                
                // translation cost between symbols ( j-1, because 0 row 
                // inserted for not aligning at beginning)
                int tr_cost = symDist(istr[i], ostr[j-1]);
                
                // skipping cost greater zero if not yet aligned 
                int sk_cost = p_sk[j]? skConst : 0; //this.skipDist(istr[i-1]):0;
                
                if ( sk_cost + p_d[j] < tr_cost + d[j-1]) {
                    // skipping cheaper
                    
                    
                    // cost is cost from previous input char + skipping
                    d[j]  = sk_cost + p_d[j];
                    // alignment is from prev. input + delimiter
                    al[j] = p_al[j] + " " + delim;
                    // yes, we skipped
                    sk[j] = true;
                    
                } else {
                    // aligning cheaper
                                
                    // cost is that from previously aligned output + distance
                    d[j]  = tr_cost + d[j-1];
                    // alignment continues from previously aligned
                    al[j] = al[j-1] + " " + ostr[j-1];
                    // nope, didn't skip
                    sk[j] = false;
                    
                }
            }
            
            // swapping
            _d  = p_d;
            p_d = d;
            d   = _d;

            _sk  = p_sk;
            p_sk = sk;
            sk   = _sk;
            
            _al  = p_al;
            p_al = al;
            al   = _al;
        }
        
        
        return p_al[ostr.length];
        
    }
    

    
    private void showLexiconAlignment(){

        for ( int i = 0; i < this.phoneStrings.length; i++ ){
            
            String alignment = this.distanceAlign(this.graphemeStrings[i], this.phoneStrings[i]);
            
            System.out.println("---");
            System.out.println(this.graphemeStrings[i]);
            System.out.println(alignment);
            
            String[] in = this.graphemeStrings[i].trim().split(" ");
            // assure that there is at least one space sign after last '#'
            String[] out = alignment.concat(" ").split("#");
            
            // for every input symbol...
            for ( int inNr = 0; inNr < in.length; inNr++){

                String outStr = out[inNr].trim();
            }
            
        }
        
    }

}
