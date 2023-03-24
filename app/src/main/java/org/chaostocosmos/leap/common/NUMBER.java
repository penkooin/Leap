package org.chaostocosmos.leap.common;

/**
 * Unit of amount of numbers
 * @author 9ins 
 */
public enum NUMBER {

    ONE(1),
    TEN(10),
    HUNDRED(100),
    THOUSAND(1000),
    TEN_THOUSAND(10000),
    HUNDRED_THOUSAND(100000),
    MILLION(1000000),
    TEN_MILLION(10000000),
    HUNDRED_MILLION(100000000),
    BILLION(1000000000),
    TEN_BILLION(10000000000L),
    HUNDRED_BILLION(100000000000L),
    TRILLION(1000000000000L),    
    Quadrillion(1000000000000000L),
    Quintillion(1000000000000000000L);

    long number;

    NUMBER(long number) {
        this.number = number;
    }
}
