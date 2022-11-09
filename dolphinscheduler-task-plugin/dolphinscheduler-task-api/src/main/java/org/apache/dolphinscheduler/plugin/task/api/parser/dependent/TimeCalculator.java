package org.apache.dolphinscheduler.plugin.task.api.parser.dependent;

import org.apache.dolphinscheduler.plugin.task.api.parser.dependent.generate.TimeCalculateLexer;
import org.apache.dolphinscheduler.plugin.task.api.parser.dependent.generate.TimeCalculateParser;
import org.apache.dolphinscheduler.plugin.task.api.parser.dependent.processor.TimeCalculateListener;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.joda.time.DateTime;

public class TimeCalculator {

    public static DateTime parse(String expr, DateTime businessTime) {
        expr = (expr == null) ? "" : expr;
        CharStream stream = CharStreams.fromString(expr);
        TimeCalculateLexer lexer = new TimeCalculateLexer(stream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        TimeCalculateParser parser = new TimeCalculateParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        ParseTree parseTree = parser.timeCalc();
        TimeCalculateListener timeCalculator = new TimeCalculateListener(businessTime);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(timeCalculator, parseTree);
        return timeCalculator.getResult();
    }

    public static DateTime parse(String expr, DateTime businessTime, String timeInit) {
        expr = (expr == null) ? "" : expr;
        CharStream stream = CharStreams.fromString(expr);
        TimeCalculateLexer lexer = new TimeCalculateLexer(stream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        TimeCalculateParser parser = new TimeCalculateParser(tokenStream);
        ParseTree parseTree = parser.timeCalc();
        TimeCalculateListener timeCalculator = new TimeCalculateListener(businessTime, timeInit);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(timeCalculator, parseTree);
        return timeCalculator.getResult();
    }

    public static void main(String[] args) {
        DateTime dt = parse("-((d+2*(M+(12*(M<3)))+3*((M+(12*(M<3)))+1)//5+(y+(M<3))+(y+(M<3))//4-(y+(M<3))//100+(y+(M<3))//400)%7)d", DateTime.now());

        System.out.println(dt);
        System.out.println(parse("yM01e::12-1", DateTime.now(),"S"));


    }
}


