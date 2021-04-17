import java.io.InputStream;
import java.io.IOException;
import java.util.*;
/*
* -------------------------------------------------------------------------
* 	        |     '0' .. '9'     |  ':'    |       '?'          |  $    |
* -------------------------------------------------------------------------
* 	        |		             |	       |	                |       |
* Tern      | '0'..'9' TernTail  |  error  |       error        | error |
*           | 	   	             |	       |    	            |       |
* -------------------------------------------------------------------------
*           |		             |	       |		            |       |
* TernTail  |       error	     |    e    |  '?' Tern ':' Tern |   e   |
* 	        |	  	             |	       |    	     	    |       |
* -------------------------------------------------------------------------
*/


class TernaryEvaluator {
    private final InputStream in;

    private int lookahead;

    public TernaryEvaluator(InputStream in) throws IOException {
        this.in = in;
        lookahead = in.read();
    }

    private void consume(int symbol) throws IOException, ParseError {
        if (lookahead == symbol)
            lookahead = in.read();
        else
            throw new ParseError();
    }

    private void consumeWhitespaces(){
        while(lookahead == ' '){
            try {
                lookahead = in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return;
    }
    private boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }

    private int evalDigit(int c) {
        return c - '0';
    }

    public int eval() throws IOException, ParseError {
        int value = EXP();
        consumeWhitespaces();

        if (lookahead != -1 && lookahead != '\n')
            throw new ParseError();

        return value;
    }

    private static int pow(int base, int exponent) {
        if (exponent < 0)
            return 0;
        if (exponent == 0)
            return 1;
        if (exponent == 1)
            return base;

        if (exponent % 2 == 0) //even exp -> b ^ exp = (b^2)^(exp/2)
            return pow(base * base, exponent/2);
        else                   //odd exp -> b ^ exp = b * (b^2)^(exp/2)
            return base * pow(base * base, exponent/2);
    }

    private int NUM() throws IOException, ParseError{
        int number=0;

        int num = evalDigit(lookahead);
        consume(lookahead);

        number=num;
        if(isDigit(lookahead) && num != 0){
            number = NUM2(num);
        }

        return number;
    }

    private int NUM2(int prev) throws IOException, ParseError{
        int num = evalDigit(lookahead);
        int number=prev;
        consume(lookahead);

        if(isDigit(lookahead)){
            number = NUM2(prev*10 + num);
        }else{
            number = prev*10 + num;
        }

        return number;
    }

    private int TERM() throws IOException, ParseError {
        consumeWhitespaces();

        if (isDigit(lookahead)) {
            int number = NUM();
            consumeWhitespaces();
            return number;
        }else if(lookahead == '('){
            consume(lookahead);
            consumeWhitespaces();

            int value;
            int term = TERM();
            value = EXP1(term);

            if(lookahead == ')'){
                consume(lookahead);
                consumeWhitespaces();
                return value;
            }
        }

        throw new ParseError();
    }

    private int EXP() throws IOException, ParseError {
        consumeWhitespaces();

        int term = TERM();

        return NEXP(term);
    }

    private int NEXP(int start) throws IOException, ParseError {
        consumeWhitespaces();

        if(lookahead == '*') {
            consume('*');
            consume('*');
            consumeWhitespaces();
            int exp = EXP2();
            return EXP1(pow(start,exp));
        }else{
            return EXP1(start);
        }
    }

    private int EXP1(int start) throws IOException, ParseError {
        int rest, term;
        switch (lookahead) {
            case '+':
                consume('+');
                consumeWhitespaces();

                term = EXP2();
                rest = EXP1(term);

                return start + rest;
            case '-':
                consume('-');
                consumeWhitespaces();

                term = EXP2();
                rest = EXP1(term*(-1));

                return start + rest;
            case -1:
            case ')':
                return start;
            case '\n':
                return start;
        }

        throw new ParseError();
    }

    private int EXP2() throws IOException, ParseError {
        consumeWhitespaces();

        int term = TERM();
        consumeWhitespaces();

        int exp = EXP3();
        consumeWhitespaces();

        return pow(term, exp);
    }

    private int EXP3() throws IOException, ParseError {

        if(lookahead == '*') {
            consume('*');
            consume('*');
            consumeWhitespaces();
            int rest = EXP2();
            return rest;
        }else if(lookahead == '\n' || lookahead == '+' || lookahead == '-' || lookahead == ')'){
            return 1;
        }

        throw new ParseError();
    }
}
