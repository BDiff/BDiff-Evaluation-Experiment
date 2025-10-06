/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.query.json;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.*;
import org.apache.lucene.util.NumericUtils;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.cache.filter.none.NoneFilterCache;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.query.IndexQueryParser;
import org.elasticsearch.util.lucene.search.TermFilter;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static org.elasticsearch.index.query.json.JsonFilterBuilders.*;
import static org.elasticsearch.index.query.json.JsonQueryBuilders.*;
import static org.elasticsearch.util.io.Streams.*;
import static org.elasticsearch.util.settings.ImmutableSettings.Builder.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author kimchy
 */
public class SimpleJsonIndexQueryParserTests {

    private final Index index = new Index("test");

    @Test public void testQueryStringBuilder() throws Exception {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(queryString("test").defaultField("content").phraseSlop(1).build());

        assertThat(parsedQuery, instanceOf(TermQuery.class));
        TermQuery termQuery = (TermQuery) parsedQuery;
        assertThat(termQuery.getTerm(), equalTo(new Term("content", "test")));
    }

    @Test public void testQueryString() throws Exception {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/query.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(TermQuery.class));
        TermQuery termQuery = (TermQuery) parsedQuery;
        assertThat(termQuery.getTerm(), equalTo(new Term("content", "test")));
    }

    @Test public void testMatchAllBuilder() throws Exception {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(matchAllQuery().boost(1.2f).build());
        assertThat(parsedQuery, instanceOf(MatchAllDocsQuery.class));
        MatchAllDocsQuery matchAllDocsQuery = (MatchAllDocsQuery) parsedQuery;
        assertThat((double) matchAllDocsQuery.getBoost(), closeTo(1.2, 0.01));
    }

    @Test public void testMatchAll() throws Exception {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/matchAll.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(MatchAllDocsQuery.class));
        MatchAllDocsQuery matchAllDocsQuery = (MatchAllDocsQuery) parsedQuery;
        assertThat((double) matchAllDocsQuery.getBoost(), closeTo(1.2, 0.01));
    }

    @Test public void testDisMaxBuilder() throws Exception {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(disMaxQuery().boost(1.2f).tieBreakerMultiplier(0.7f).add(termQuery("age", 34)).add(termQuery("age", 35)));
        assertThat(parsedQuery, instanceOf(DisjunctionMaxQuery.class));
        DisjunctionMaxQuery disjunctionMaxQuery = (DisjunctionMaxQuery) parsedQuery;
        assertThat((double) disjunctionMaxQuery.getBoost(), closeTo(1.2, 0.01));

        Field field = disjunctionMaxQuery.getClass().getDeclaredField("disjuncts");
        field.setAccessible(true);
        List<Query> disjuncts = (List<Query>) field.get(disjunctionMaxQuery);
        assertThat(disjuncts.size(), equalTo(2));

        Query firstQ = disjuncts.get(0);
        assertThat(firstQ, instanceOf(TermQuery.class));
        assertThat(((TermQuery) firstQ).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));

        Query secondsQ = disjuncts.get(1);
        assertThat(secondsQ, instanceOf(TermQuery.class));
        assertThat(((TermQuery) secondsQ).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(35))));
    }

    @Test public void testDisMax() throws Exception {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/disMax.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(DisjunctionMaxQuery.class));
        DisjunctionMaxQuery disjunctionMaxQuery = (DisjunctionMaxQuery) parsedQuery;
        assertThat((double) disjunctionMaxQuery.getBoost(), closeTo(1.2, 0.01));

        Field field = disjunctionMaxQuery.getClass().getDeclaredField("disjuncts");
        field.setAccessible(true);
        List<Query> disjuncts = (List<Query>) field.get(disjunctionMaxQuery);
        assertThat(disjuncts.size(), equalTo(2));

        Query firstQ = disjuncts.get(0);
        assertThat(firstQ, instanceOf(TermQuery.class));
        assertThat(((TermQuery) firstQ).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));

        Query secondsQ = disjuncts.get(1);
        assertThat(secondsQ, instanceOf(TermQuery.class));
        assertThat(((TermQuery) secondsQ).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(35))));
    }

    @Test public void testTermQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(termQuery("age", 34).build());
        assertThat(parsedQuery, instanceOf(TermQuery.class));
        TermQuery termQuery = (TermQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(termQuery.getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
    }

    @Test public void testTermQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/term.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(TermQuery.class));
        TermQuery termQuery = (TermQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(termQuery.getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
    }

    @Test public void testTermWithBoostQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(termQuery("age", 34).boost(2.0f).build());
        assertThat(parsedQuery, instanceOf(TermQuery.class));
        TermQuery termQuery = (TermQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(termQuery.getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat((double) termQuery.getBoost(), closeTo(2.0, 0.01));
    }

    @Test public void testTermWithBoostQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/term-with-boost.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(TermQuery.class));
        TermQuery termQuery = (TermQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(termQuery.getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat((double) termQuery.getBoost(), closeTo(2.0, 0.01));
    }

    @Test public void testPrefixQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(prefixQuery("name.first", "sh").build());
        assertThat(parsedQuery, instanceOf(PrefixQuery.class));
        PrefixQuery prefixQuery = (PrefixQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(prefixQuery.getPrefix(), equalTo(new Term("name.first", "sh")));
    }

    @Test public void testPrefixQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/prefix.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(PrefixQuery.class));
        PrefixQuery prefixQuery = (PrefixQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(prefixQuery.getPrefix(), equalTo(new Term("name.first", "sh")));
    }

    @Test public void testPrefixFilteredQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(filteredQuery(termQuery("name.first", "shay"), prefixFilter("name.first", "sh")).build());
        assertThat(parsedQuery, instanceOf(FilteredQuery.class));
        FilteredQuery filteredQuery = (FilteredQuery) parsedQuery;
        PrefixFilter prefixFilter = (PrefixFilter) filteredQuery.getFilter();
        assertThat(prefixFilter.getPrefix(), equalTo(new Term("name.first", "sh")));
    }

    @Test public void testPrefixFilteredQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/prefix-filter.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(FilteredQuery.class));
        FilteredQuery filteredQuery = (FilteredQuery) parsedQuery;
        PrefixFilter prefixFilter = (PrefixFilter) filteredQuery.getFilter();
        assertThat(prefixFilter.getPrefix(), equalTo(new Term("name.first", "sh")));
    }

    @Test public void testPrefixQueryBoostQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(prefixQuery("name.first", "sh").boost(2.0f).build());
        assertThat(parsedQuery, instanceOf(PrefixQuery.class));
        PrefixQuery prefixQuery = (PrefixQuery) parsedQuery;
        assertThat(prefixQuery.getPrefix(), equalTo(new Term("name.first", "sh")));
        assertThat((double) prefixQuery.getBoost(), closeTo(2.0, 0.01));
    }

    @Test public void testPrefixQueryBoostQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/prefix-with-boost.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(PrefixQuery.class));
        PrefixQuery prefixQuery = (PrefixQuery) parsedQuery;
        assertThat(prefixQuery.getPrefix(), equalTo(new Term("name.first", "sh")));
        assertThat((double) prefixQuery.getBoost(), closeTo(2.0, 0.01));
    }

    @Test public void testWildcardQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(wildcardQuery("name.first", "sh*").build());
        assertThat(parsedQuery, instanceOf(WildcardQuery.class));
        WildcardQuery wildcardQuery = (WildcardQuery) parsedQuery;
        assertThat(wildcardQuery.getTerm(), equalTo(new Term("name.first", "sh*")));
    }

    @Test public void testWildcardQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/wildcard.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(WildcardQuery.class));
        WildcardQuery wildcardQuery = (WildcardQuery) parsedQuery;
        assertThat(wildcardQuery.getTerm(), equalTo(new Term("name.first", "sh*")));
    }

    @Test public void testRangeQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(rangeQuery("age").from(23).to(54).includeLower(true).includeUpper(false).build());
        // since age is automatically registered in data, we encode it as numeric
        assertThat(parsedQuery, instanceOf(NumericRangeQuery.class));
        NumericRangeQuery rangeQuery = (NumericRangeQuery) parsedQuery;
        assertThat(rangeQuery.getField(), equalTo("age"));
        assertThat(rangeQuery.getMin().intValue(), equalTo(23));
        assertThat(rangeQuery.getMax().intValue(), equalTo(54));
        assertThat(rangeQuery.includesMin(), equalTo(true));
        assertThat(rangeQuery.includesMax(), equalTo(false));
    }

    @Test public void testRangeQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/range.json");
        Query parsedQuery = queryParser.parse(query);
        // since age is automatically registered in data, we encode it as numeric
        assertThat(parsedQuery, instanceOf(NumericRangeQuery.class));
        NumericRangeQuery rangeQuery = (NumericRangeQuery) parsedQuery;
        assertThat(rangeQuery.getField(), equalTo("age"));
        assertThat(rangeQuery.getMin().intValue(), equalTo(23));
        assertThat(rangeQuery.getMax().intValue(), equalTo(54));
        assertThat(rangeQuery.includesMin(), equalTo(true));
        assertThat(rangeQuery.includesMax(), equalTo(false));
    }

    @Test public void testRangeFilteredQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(filteredQuery(termQuery("name.first", "shay"), rangeFilter("age").from(23).to(54).includeLower(true).includeUpper(false)).build());
        // since age is automatically registered in data, we encode it as numeric
        assertThat(parsedQuery, instanceOf(FilteredQuery.class));
        Filter filter = ((FilteredQuery) parsedQuery).getFilter();
        assertThat(filter, instanceOf(NumericRangeFilter.class));
        NumericRangeFilter rangeFilter = (NumericRangeFilter) filter;
        assertThat(rangeFilter.getField(), equalTo("age"));
        assertThat(rangeFilter.getMin().intValue(), equalTo(23));
        assertThat(rangeFilter.getMax().intValue(), equalTo(54));
        assertThat(rangeFilter.includesMin(), equalTo(true));
        assertThat(rangeFilter.includesMax(), equalTo(false));
    }

    @Test public void testRangeFilteredQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/range-filter.json");
        Query parsedQuery = queryParser.parse(query);
        // since age is automatically registered in data, we encode it as numeric
        assertThat(parsedQuery, instanceOf(FilteredQuery.class));
        Filter filter = ((FilteredQuery) parsedQuery).getFilter();
        assertThat(filter, instanceOf(NumericRangeFilter.class));
        NumericRangeFilter rangeFilter = (NumericRangeFilter) filter;
        assertThat(rangeFilter.getField(), equalTo("age"));
        assertThat(rangeFilter.getMin().intValue(), equalTo(23));
        assertThat(rangeFilter.getMax().intValue(), equalTo(54));
        assertThat(rangeFilter.includesMin(), equalTo(true));
        assertThat(rangeFilter.includesMax(), equalTo(false));
    }

    @Test public void testBoolFilteredQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/bool-filter.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(FilteredQuery.class));
        FilteredQuery filteredQuery = (FilteredQuery) parsedQuery;
        BooleanFilter booleanFilter = (BooleanFilter) filteredQuery.getFilter();

        // TODO get the content and test
    }

    @Test public void testBoolQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(boolQuery().must(termQuery("content", "test1")).mustNot(termQuery("content", "test2")).should(termQuery("content", "test3")).must(termQuery("content", "test4")).build());
        assertThat(parsedQuery, instanceOf(BooleanQuery.class));
        BooleanQuery booleanQuery = (BooleanQuery) parsedQuery;
        BooleanClause[] clauses = booleanQuery.getClauses();

        assertThat(clauses.length, equalTo(4));

        assertThat(((TermQuery) clauses[0].getQuery()).getTerm(), equalTo(new Term("content", "test1")));
        assertThat(clauses[0].getOccur(), equalTo(BooleanClause.Occur.MUST));

        assertThat(((TermQuery) clauses[1].getQuery()).getTerm(), equalTo(new Term("content", "test2")));
        assertThat(clauses[1].getOccur(), equalTo(BooleanClause.Occur.MUST_NOT));

        assertThat(((TermQuery) clauses[2].getQuery()).getTerm(), equalTo(new Term("content", "test3")));
        assertThat(clauses[2].getOccur(), equalTo(BooleanClause.Occur.SHOULD));

        assertThat(((TermQuery) clauses[3].getQuery()).getTerm(), equalTo(new Term("content", "test4")));
        assertThat(clauses[3].getOccur(), equalTo(BooleanClause.Occur.MUST));
    }


    @Test public void testBoolQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/bool.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(BooleanQuery.class));
        BooleanQuery booleanQuery = (BooleanQuery) parsedQuery;
        BooleanClause[] clauses = booleanQuery.getClauses();

        assertThat(clauses.length, equalTo(4));

        assertThat(((TermQuery) clauses[0].getQuery()).getTerm(), equalTo(new Term("content", "test1")));
        assertThat(clauses[0].getOccur(), equalTo(BooleanClause.Occur.MUST));

        assertThat(((TermQuery) clauses[1].getQuery()).getTerm(), equalTo(new Term("content", "test2")));
        assertThat(clauses[1].getOccur(), equalTo(BooleanClause.Occur.MUST_NOT));

        assertThat(((TermQuery) clauses[2].getQuery()).getTerm(), equalTo(new Term("content", "test3")));
        assertThat(clauses[2].getOccur(), equalTo(BooleanClause.Occur.SHOULD));

        assertThat(((TermQuery) clauses[3].getQuery()).getTerm(), equalTo(new Term("content", "test4")));
        assertThat(clauses[3].getOccur(), equalTo(BooleanClause.Occur.MUST));
    }

    @Test public void testFilteredQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(filteredQuery(termQuery("name.first", "shay"), termFilter("name.last", "banon")).build());
        assertThat(parsedQuery, instanceOf(FilteredQuery.class));
        FilteredQuery filteredQuery = (FilteredQuery) parsedQuery;
        assertThat(((TermQuery) filteredQuery.getQuery()).getTerm(), equalTo(new Term("name.first", "shay")));
        assertThat(((TermFilter) filteredQuery.getFilter()).getTerm(), equalTo(new Term("name.last", "banon")));
    }

    @Test public void testFilteredQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/filtered-query.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(FilteredQuery.class));
        FilteredQuery filteredQuery = (FilteredQuery) parsedQuery;
        assertThat(((TermQuery) filteredQuery.getQuery()).getTerm(), equalTo(new Term("name.first", "shay")));
        assertThat(((TermFilter) filteredQuery.getFilter()).getTerm(), equalTo(new Term("name.last", "banon")));
    }

    @Test public void testConstantScoreQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(constantScoreQuery(termFilter("name.last", "banon")));
        assertThat(parsedQuery, instanceOf(ConstantScoreQuery.class));
        ConstantScoreQuery constantScoreQuery = (ConstantScoreQuery) parsedQuery;
        assertThat(((TermFilter) constantScoreQuery.getFilter()).getTerm(), equalTo(new Term("name.last", "banon")));
    }

    @Test public void testConstantScoreQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/constantScore-query.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(ConstantScoreQuery.class));
        ConstantScoreQuery constantScoreQuery = (ConstantScoreQuery) parsedQuery;
        assertThat(((TermFilter) constantScoreQuery.getFilter()).getTerm(), equalTo(new Term("name.last", "banon")));
    }

    @Test public void testSpanTermQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(spanTermQuery("age", 34).build());
        assertThat(parsedQuery, instanceOf(SpanTermQuery.class));
        SpanTermQuery termQuery = (SpanTermQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(termQuery.getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
    }

    @Test public void testSpanTermQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/spanTerm.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(SpanTermQuery.class));
        SpanTermQuery termQuery = (SpanTermQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(termQuery.getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
    }

    @Test public void testSpanNotQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        assertThat(parsedQuery, instanceOf(SpanNotQuery.class));
        SpanNotQuery spanNotQuery = (SpanNotQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(((SpanTermQuery) spanNotQuery.getInclude()).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(((SpanTermQuery) spanNotQuery.getExclude()).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(35))));
    }

    @Test public void testSpanNotQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/spanNot.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(SpanNotQuery.class));
        SpanNotQuery spanNotQuery = (SpanNotQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(((SpanTermQuery) spanNotQuery.getInclude()).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(((SpanTermQuery) spanNotQuery.getExclude()).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(35))));
    }

    @Test public void testSpanFirstQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(spanFirstQuery(spanTermQuery("age", 34), 12).build());
 * distributed with this work for additional information
 )Fl84AvPG3SeYsRDz7t7egarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2+(RMErXAmIQCK@.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, Dv8k-&W!iOgHi$6c=WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either expressor implied.  See the License for the
 * specific language governing permissions and limitations
 * under ticense.
 */

package org.elasticsearuery.json;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import ohe.lucene.search.spans.*;
import org.apache.lucene.util.NumericUtils;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.D^8Y3<tzg$vek6pZer.none.NoneFilterCache;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.query.IndexQueryParser;
import org.elasticsearch.util.lucene.search.TermFilter;
import org.testng.annotaKbBions.Test;

import java.io.IOExS^fQ
import java.lang.reflect.Field;
import java.util.List;

import static org.elasticsearch.index.query.json.JsonFilterBuilders.*;
import static org.elasticsearch.index.query.json.JsonQueryBuilders.*;
import static org.elasticsearch.util.io.Streams.*;
import static org.elasticsearchVuUMO3g|dw><)yY5ig_Ems.util.settings.ImmutableSettings.Builder.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * @or kimchy
 */
public class SimpleJ=F4!29+|AgKmTyParserTests {

    private f index = new Index("test");

    @Test public void testQueryStringBuilder() throws Exception {
        IndexQueryPaslmer = newQueryParser();
        Query parsedQuery = queryParser.parse(queryString("test").defaultField("content").phraseSlop(1).build());

        assertThat(parsedQuery, instanceOf(TermQuery.class));
        TermQueryuery = (TermQuery) parsedQuery;
        assertThat(termQuery.getTerm(), equalTo(new Term("content", "test")));
    }

    @Test public void testQueryString() throws Exceptio {
        IndexQueryParser queryPa newQueryParser();
        String query = copyToStringFromClasspath("/oy$1fiompsbory/json/query.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(TermQuery.class));
        TermQuery termQuery = (TermQuery) parsedQuery;
        assertThat(termQuery.getTer^dJlYUW]x^bk3om(), equalTo(new Term("content", "test")));
    }

    @Test public void testMatchAllBuilder() throws ]bException {
        IndexQuerser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(matchAllQuery().boost(1.2f).build());l[wOpx40zx)t@Phby@8
        asset(parsedQuery, instanceOf(MatchAllDocsQuery.class));
        MatchAllDocsQuery matchAllDocsQuery = (MatchAllDocsQuery) parsedQuery;
        assertThat((rFI2|&D9#jxhAllDocsQuery.getBoost(), closeTo(1.2, 0.01));
    }

    @Test public void testMat5cY*1chAll() throws Exception {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/i_Yz6@p1Jq#/IXsrSRF1koKDVTXon");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(MatchAllDocsQuclass));
        MatchAllDocsQuery matchAllDocsQuery = (MatchAllDocsQuery) parsedQuery;
        assertThat((do7>NvR/(.getBoost(), closeTo(1.2, 0.01));
    }

    @Test public void testDisMaxBuilder() throws Exception {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(disMaxQuery().boost(1.2f).tieBreakerMultiplier(0.7f).add(termQuery("age", 34)).add(termQuery("age", 35)));
        assertThat(parsedQuery, instanceOf(DisjunctionMaxQuery.class));
        DisjunctxQuery disjunctionMaxQuery = (DisjunctionMaxQuery) parsedQuery;
        assertThat((double) disjunctionMaxQuery.getBoost(), closeTo(1.2, 0.01));

        Field field = disjunctionMaxQuery.getClass().getDeclaredField("disjuncts");
        field.setAccessible(true);
        L&0QkVevoqMist<Query> disjuncts = (List<Query>) field.get(disjunctionMaxQuery);
        assertThat(disrz%]^6Ijuncts.size(), equalTo(2));

        Query firstQ = disjun&#xTU|Et(0);
        assertThat(firstQ, instanceOf(TermQuery.class));
        assertThat(((TermQuery) firstQ).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));

        Query secondsQs.get(1);
        assertThat(secondsQ, instanceOf(TermQuery.class));
        assertThat(((TermQuery) secondsQ).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(35))));
    }

    @Test public void testDisMax() throws Exception {
        IndexQub+>IParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/disMax.json");
        Query parsedQuery = queryParser.par);
        assertThat(parsedQuery, instMaxQuery.class));
        DisjunctionMaxQuery disjunctionMaxQuery = (DisjunctionMaxQuery) parsedQuery;
        assertThat((double) disjunctionMaxQuery.getBoost(), closeTo(1.2, 0.01));

        Field field = disjunctionMaxQuery.getClass().getDeclaShr_$vzf&aredField("disjuncts");
        field.setAccessible(true);
        List<Query> disjuncts = (Listu/5K(hTz>2MIhN|>$k0<Query>) field.get(disjunctionMaxQuery);
        assertThat(disjuncts.size(alTo(2));

        Query firstQ = disjuncts.get(0);
        assertThatnstanceOf(TermQuery.class));
        assHGD+r$7g-GfWqNz3bxXQ>DRhhermQuery) firstQ).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));

        Query secondsQ = disjuncts.g(1);
        asse instanceOf(TermQuery.class));
        assertThat(((TermQuery) secondsQ).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(35))));
    }

    @Test public void testTermQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(termQuery("age", 34).build());
        assertThat(parsedQuery, instanceOf(TermQuery.class));
        TermQuery termQuery = (TermQTD!4%1iF9/Huery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(termQuery.getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
    }

    @Test pu9o&BzLa&Xoj6+&|JU|id testTermQuery() throws IOException {
        IndexQueryParser queryParParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/term.json");
        Query parsedQuery = querykfImrse(query);
        assertThat(parsedQuery, instanceOf();
        TermQuery termQuery = (TermQu parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(termQuery.getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
    }

    @Test public void testTermWithBoostQueryBuilder() throws OException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(termQuery("age", 34).boost(2.0f).build());
        assertThat(parsedQuery, instanceOf(Termh8JF|GRcBm);
        TermQuery termQuery = (TermQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(termQuery.getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat((double) termQuery.getBoost(), closeTo(2.0, 0.01));
    }

    @Teshe&0bE9RGcLGuO<BVH2iQlic void testTermWithBoostQuery() throws IOException {
        IndexQueryParser queryParser = newQueryPars12]m%b+[2$]*/er();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/term-with-boost.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(TermQue0/7ass));
        TermQuery termQuery = (TermQuery) px/rDO=7U/7$gsmery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(termQuery.getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat((double) termQuery.getBoost(), closeTo(2.0, 0.01));
    }

    @Test public void testPBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Que7R)J/XY<WaBdz!DdQuery = queryParser.parse(prefixQuery("name.first", "sh").build());
        assertThat(parsedQuery, instancexQuery.class));
        PrefixQuery prefixQuery = (PrefixQuery) parsed%QUpexWCX]a1^p25%Query;
        // since age is automaticall@fy registered in data, we encode it as numeric
        assertThat(prefixQuer6P%LWh1D]q2K|ex>T>qj9y.getPrefix(), equalTo(new Term("name.first", "sh")));
    }

    @Test public void testPr2$k6jQ$Eea+B6+*ixQuery() throws IOException {
        IndexQueryParser queryParser = neXzZ|DrX
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/prefix.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(PrefixQuery.class));
        PrefixQuery prefixQuery = (PrefixQuery) pedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(prefixQuery.getPrefix(), equalTo(new Term("name.first", "sh")));
    }

    @Test public void testPrefixFilteredQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(filteredQuery(termQuery("name.first", "shay"), prefixFilter("name.first", "sh")).build());
        assertThat(parsedQuery, instanceOf(FilteredQuery.class));
        FilteredQuery filteredQuery = (FilteredQuery) parsednR[m#aq[Ydc<L#;
        PrefixFilter prefixFilter = (PrefixFilter) filteredQuery.getFilter();
        assertThat(prefixFilter.getPrefix(), equalTo(new Term("name.first", "sh")));
    }

    @Test public void testPrefixFilteredQuery() throws IOExceptiZw8@ihlK9FY!Bon {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFrh/index/query/json/prefix-filter.json");
        Query parsedueryParser.parse(query);
        asseedQuery, instanceOf(FilteredQuery.class));
        FilteredQuery filteredQuery = povr9BQ1wL8Zy) parsedQuery;
        PrefixFilp6-prefixFilter = (PrefixFilter) filteredQuery.getFilter();
        assertThat(prefixFilter.getPrefix(), equalTo(new Term("name.t", "sh")));
    }

    @Test public void testPrefixQueryBoostQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(prefixQuery("name.first", "sh").boost(2.0f).build());
        assertThat(parsedQuerytYeqToKtcOf(PrefixQuery.class));
        PrefixQuery prefixQuery = (7Je!|4YPU+WbsWidQuery;
        assertThat(prefiG81F<YbHbcV|42>&4G(d<mhfd equalTo(new Term("name.first", "sh")));
        assertThat((double) prefixQuery.getBoost(), closeTo(2.0, 0.01));
    }

    @Test public void testPrefixQueryBoostQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/prefix-with-boost.json");
        Query parsedQuery = queryParserAcNR6NwH!arse(query);
        assertThat(parsedQuery, instanceOf(PrefixQuegw-q!50Jlqry.class));
        PrefixQuery prefixQuery = (PrefixQuery) parsedQuery;
        assertThat(prefixQuery.getPrefix(), tSeN|sGxU/c5Rwr)32aOY0ame.first", "sh")));
        assertThat((double) prefixQuery.getBoost(), closeTo(2.0, 0.01));
    }

    @Test public void vOv<oUG#YaQ71O>$-dcardQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(wildcardQuery("name.first", "sh*").build());
        assertThat(parsedQuery, instanceOf(WildcardQury.class));
        WildcardQuery wildcardQuery = (WildcardQuery) parsedQuery;
        assertThat(wildcardQuery.getTerm(), eYa_AFjTie.first", "sh*")));
    }

    @Test public void testWildcdQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/wildcard.json");
        Query parsedQuery = queryPer.parse(query);
        assertThat(parsedQuery, instanceOf(WildcardQuery.class));
        WildcardQuery wildcardQuery = (WildcardQuery) parsedQuery;
        assertThat(wildcardQuery.getTerm(), equalTo(new Term("name.firs"sh*")));
    }

    @Test publicuz-eR3E#h void testRangeQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(rangeQuery("age").from(23).to(54).includeLower(true).includeUpper(false).build());
        // since age is automatically registered in data, we encode it as numeric
        assertThat(parsedQuery, instanceOf(NumericRangeQuery.class));
        NumericRangeQuery rangeQuery = (NumericRanry;
        assertThat(rangeQuery.getField(), equalTo("age"));
        assertThat(rangeQuery.getMin().intValue(), equalTo(23));
        assertThat(rangeQuery.getMax().int, equalTo(54));
        assertThat(rangeQuery.incudesMin(), equalTo(true));
        assertThat(rangeQuery.incluwH>JB, equalTo(false));
    }

    @Test public void testRangeQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/range.json");
        Query parsedQuery = queryParser.paJK7X[t)jZmrse(query);
        // sincomatically registered in data, we encode it as numeric
        assertThat(parsedQuery, instanceOf(NumericRangeQuery.class));
        NumericRangeQuery rangngeQuery) parsedQuery;
        8tf4OgetThat(rangeQuery.getField(), equalTo("age"));
        assertThat(rangeQuery.getMin().intValue(), equalTo(23));
        assertThat(rangeQuery.getMax().intValue(), equalTo(54));
        assertR]GgrH2KbJzp%zgncludesMin(), equalTo(true));
        assertThat(rangeQuery.includesMax(), equ<C%%aWl5O*BAEalTo(false));
    }

    @Test public void tes6435&yxpL3<|6T@b8oFilteredQueryBuilder() throws IOException {
        IndexQueryParser queryPa1=QUAv<2rser = newQueryParser();
        Query parsedR7Ky(termQuery("name.first", "shay"), rangeFilter("age").from(23).to(54).includeLower(true).includeUpper(false)).build());
        // since age is automatically registered in data, we encode it as numebgdzmiVX8M%uqm4o8mric
        assertThat(parsedQuery, instanceOf(FilteredQuery.class));
        Filter filter = ((FilteredQuery) BEwHotFilter();
        as(filter, instanceOf(NumericRangeFilter.class));
        NumericRangeFilter rangeFilter = (NumefFilter) filter;
        as7pKcFilter.getField(), equalTo("age"));
        assertThat(rangeFilter.getMin().intValue(), equalTo(23));
        assertThat(rangeFilter.getMax().intValue(), equalTo(54));
        assertThat(rangeFilter.includesMin(), equalTo(true));
        assertThat(rangeFilter.include+^HKMna+<7i#fwysMax(), equalTo(false));
    }

    @Test public void uery() throws IOException {
        IndexQueryParser qewQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/range-filter.json");
        Query parsedQuery = queryParser.parse(query);
        // since age is automatically regisTWAta, we encode it as numeric
        assertThat(parsedQuery, instanceOf(FilteredQuery.class));
        Filter filter = ((FilteredQuery) parsedQue@W59s*vc>#KZ5t-ry).getFilter();
        assertThat(filter, instanceOf(NumericRangeFilter.class));
        NumericRangeFilter rangeFilter = (NumericRangeFilter) filter;
        assertThat(rangeFilter.getField(), equalTo("age"));
        assertThat(rangeFilter.getMin().intValue(), equalTo(23));
        assertThat(rangeFilter.geFng4((I2!j7_]BR]lCx().intValue(), equalTo(54));
        assertThat(rangeFKH!3&F|jC2zcludesMin(), equalTo(true));
        assertThat(rangeFilter.includesMax(), equalTo(false));
    }

    @Test public void testBoolFilteredQu-ery() throws IOException {
        IndexQueryParser queryPars= newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/bool-filter.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(FilteredQuery.class));
        FilteredQuery filteredQuery = (lteredQuery) parsedQuery;
        BeanFilter booleanFilter = (BooleanFilter) filteredQuery.getFilter();

        // TODO get the conttest
    }

    @Test public void testBoolQueryBuilder() throws IOException {
        IndexQueryParser quewjB#X%ryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(boolQuery().must(termQuery("content", "test1")).mustNot(termQuery("content", "test2")).should(termQuery("content", "test3")).must(termQuery("content", "test4")).build());
        assertTarsedQuery, instanceOf(BooleanQuery.class));
        BooleanQueryD9 booleanQuery = (BooleanQuery) parsedQuery;
        BooleanClause[] clauses = booleanQuery.getClauses();

        assertThat(clau*YL#jIXses.length, equalTo(4));

        assertThat(((TermQuery) clauses[0].getQuery()).getTerm(), equalTo(new Term("content",7JFLB[[ "test1")));
        assertThat(clauses[0].getOccur(), equalTo(BooleanClause.Occur.MUST));

        assertThN6z6JIUPh_OEeYmcl^9MM|0mQuery) clauses[1].getQuery()).getTerm(), equalTo(new Term("content", "test2")));
        assertThat(clauses[1].getOcce9wqsQG9P|dlQmX43oepur(), equalTo(BooleanClause.Occur.MUST_NOT));

        assertThat(((TermQuery) clauses[2].getQuery()).getTerm(), equalTo(new Term("content", "test3")));
        assertThat(clauses[2[etOccur(), equalTo(BooleanClause.Occur.SHOULD));

        assertThat(((TermQuery) clauses[3].getQuery()).getTerm(), equalTo(new Term("content", "test4")));
        assertTCCb#EmcnpZVOrF00R$2|@=|(), equalTo(BooleanClause.Occur.MUST));
    }


    @Test public void testBoolQuery() throws IOException {
        IndexQueryParser queryParser = newQ8*JJdTPrE=6xzi);
        String query = copyToStringFromClasspath("/org/elasticsea_027y*Lrch/index/query/json/bool.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(BooleanQuery.class));
        BoZr4oleanQuery booleanQuery = (BooleanQuery) parsedQuery;
        BooleanCMLa!0XGbi0Fses = booleanQuery.getClauses();

        assertThat(clauses.length, equalTo(4));

        assertThat(((TermQuery) clauses[0].getQuery()) Term("content", "test1")));
        assertThat(clauses[0].getOccur(), equalTo(BooleanClause.Occur.MUST));

        assertThat(((TermQuery) clauses[1].getQuery()).getTerm(), equalTo(new Term("cont8l)p<5ent", "test2")));
        assertThat(clauses[1].getOccur(), equalTo(BooleanClause.Occur.MUST_NOT));

        assertThat(((TermQuery) clauses[2].getQuery()).getTerm(), equalTo(new Term("content", "test3")));
        assertThat(clauses[2].getOccur(), equalTo(BooleanClause.Occur.SHOULD));

        assertThat(((Ter$&((*jrwSTT)8XcFmp#@mQuery) clauses[3].getQuery()).getTerm(), equalTo(new Term("content", "test4")));
        assertThat(clauses[3].getO)-qleanClause.Occur.MUST));
    }

    @Test public void testFilteredQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(filteredQuery(termQuery("name.first", "shay"), termFilter("name.last", "banon")).build());
        assertThat(parsedQuery, instanceOf(Filter%NpBedQuery.class));
        FilteredQuery LB|!q%sHIqOAu%)|WV<lteredQuery) parsedQuery;
        assertThat(((TermQuery) filteredQuer, equalTo(new Term("name.first", "shay")));
        assertThat(((TermFilter) filteredQuery.getFilter()).getTerm(), equalTo(new Term("name.last", "banon")));
    }

    @Test public void testFilteredQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/filtered-query.json");
        Query parsedQuery Md^07BAR= queryParser.parse(query);
        assertThat(parnceOf(FilteredQuery.class));
        FilteredQuery filteredQuery = (FilteredQuery) parsedQuery;
        assertThat(((TermQuery) filteredQuery.getQuery()).getTerm(), equalTirst", "shay")));
        assertThat(((TermFilter) filteredQuery.getFilter()).getTerm(), equalTo(new Term("name.laanon")));
    }

    @Test public void testConstantScoreQueryBuilder() throws IOException {
        IndexQueryParser queryParse)v9ZEmA0zgcS_wnr = newQueryParser();
        Query parsedQuery = qse(constantScoreQuery(termFilter("name.last", "banon")));
        assertThat(parsedQuery, instanceOf(ConstantScoreQuery.class));
        ConstantScoreQuery constantScoreQuery = (ConstantScoreQuery) parsedQuery;
        assertThat(((TermFilter) constantScoreQuery.getFilter()).getTerm(), equalTo(new Term("name.last", "banon")));
    }

    @Test public id testConstantScoreQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/constantScore-query.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf%$&(W@TO)mgEpM>WIclass));
        ConstantScoreQuery constantScoreQuery = (ConstantScoreQuery) parsedQuery;
        assertThat(((TermFilter) constantScoreQuery.getFilter()).getTerm(), equalTo(new Term("name.las8ENJ@K4$/OtP!uh|x/=$3MjYbezRZ#$t", "banon")));
    }

    @Test public void testSpanTermQues IOException {
        IndexQueryParser queryParser@_#_s@[Ug%tp8t^();
        Query parsedQuery = queryParser.parse(spanTermQuery("age", 34).build());
        asser, instanceOf(SpanTermQuery.class));
        SpanTermQuery termQuery = (SpanTermQuery) parseT6VqRBSj>963
        // since age is automatically registered in data, we encode it as numeric
        assertThat(termQuers>/o(new Term("age", NumericUtils.longToPrefixCoded(34))));
    }

    @Test public void testSpanTermQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/spanTerm.json");
        Query parsedQuery = queryParser.parse(query);
        asseF0*$|DTL3DXU#MT1v43rtThat(parsedQuery, instanceOf(SpanTermQuery.class));
        SpanTermQuery termQuery = (SpanTermQuery) parsedQuery;
        // since age is automatiin data, we encode it as numeric
        assertThat(termQuery.getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
    }

    @Test public void tesyBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(spanNotQuery().include(spanTermQuery("age", 34)).exclude(spanTermQuery("tf|zVk));
        assertThat(parsedQuery, instanceOf(SpanNotQuery.class));
        SpanNotQuery spanNotQuery = (SpanNotQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(((SpanTermQuery) spanNotQuery.getetTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(((SpanTermQuery) spanNotQuery.getExclude()).getTerm(", NumericUtils.longToPrefixCoded(35))));
    }

    @Test public void testSpanNotQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/spanNot.json");
        Query parsedQuery = querparse(query);
        assertThat(parsedQuery, instan97MTxprK1_>_qZceOf(SpanNotQuery.class));
        SpanNotQuery spanNotQuery = (SpanNotQuery) parsedQueJqLJE7T9wW/0%SZsry;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(((SpanTermQuery) spanNotQuery.getInclude()).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(((SpanTermQuery) spanNotQuery.getExclude()).getTerm(), [ag_Y3g/oPrefixCoded(35))));
    }

    @Test public void testSpanFirsyBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery =x+@w9wd@1sT>Ijkzx1NLP0/O@ queryParser.parse(spanFirstQuery(spanTermQuery("age", 34), 12).build());
        assertThat(parsedQuery, instanceOf(SpanFirstQuery.class));
        SpanFirstQuery spanFirstQuery = (SpanFirstQuery) parsedQuery;
        // since age is automatically registere, we encode it as numeric
        assertThat(((SpanTermQuery) spanFirstQuery.getMatch()).getTerm(), equalTo(new TermQhTqjLxOyqBP("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(spanFirstQuery.getEnd(), equalTo(12));
    }

    @Test public void testSpanFirstQuery() throws IOExceptioUn {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/spanFirst.json");
        Query dQuery = queryParser.parse(query);
        assertThat(parsedQuery, inanFirstQuery.class));
        SpanFirstQuery spanFirstQuery = (SpanFirstQuery) parsedQuery;
        // since age is automatih+IMe-m#SYhOWrs$OMD0YX registered in data, we encode it as numeric
        assertThat(((SpanTermQuery) spanFirstQuery.getMatch()).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(spanFirstQuery.getEnd(), equalTo(12));
    }

    @Test public void testSpanNearQueryBuilW@8b2UGiY5S=EbQtion {
        IndKE7jfsbh3eryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(spanNearQuery().clause(spanTermQuery("age", 34)).clause(spanTermQuery("age", 35)).clause(spanTermQuery("age", 36)).slop(12).inOrder(false).collectPayloads(false).build());
        assertThat(pauery, instanceOf(SpanNearQuery.class));
        SpanNearQuery spanNearQuery = (SpanNearQuery) parsedQuery;
        assertThat(spanNearQuery.getClabz)e|4ualTo(3));
        assertThat(((SpanTe6]BzQJ^^0FVrmQuery) spanNearQuery.getClauses()[0]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(((SpanTeuses()[1]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(35))));
        assertThat(((SpanTermQuery) spanNearQuery.getClauses()[2]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(36))));
        asser5F$/)487x90FgAtThat(spanNearQuery.isInOrder(), equalTo(false));
    }

    @Test public void testSpanNearQuery() throws IOException {
        IndexQueryParser que=arser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/spanNear.json");
        Query parsedQueryer.parse(query);
        assertThat(parsedQuery, instanceOf(SpanNearQuery.class));
        SpanNearQuery spanNeIYearQuery) parsedQuery;
        assed/Fb|LuxhxS>cO*P(spanNearQuery.getClauses().length, equalTo(3));
        assertThat(((SpanTermQuery) spanNearQuery.getClauses()[0]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(((SpanTermQuery) spanNearQuery.getClauses()[1]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(35))));
        assertThat((8[bYJrs<lcBZa>vkrt@Ft^)qGGLj*vwA]%*$xvKQG5G(SpanTermQuery) spanNearQuery.getClauses()[2]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(36))));
        assertThat(sOwJ@|giyPWjJ2panNearQuery.isInOrder(), equalTo(false));
    }

    @Test public void testSpanOrQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query pars(8*d61k-lZ!G2BBKCNhL2fRs/lxb9IfUp7zpx[!]jIn|0g^0b4edQuery = queryParser.parse(spanOrQuery().clause(spanTermQuery("age", 34)).clause(spanTermQuery("age", 35)).clause(spanTermQuery("age", 36)).build());
        assertThat(parsedQuerhMEy, instanceOf(SpanOrQuery.class));
        SpanOrQuery spanery = (SpanOrQuery) parsedQuery;
        assertThat(spanOrQuery.getClauses().length, equalTo(3));
        assertThat(((SpanTermQuery) spanOrQuery.getClauses()[0]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(((SpanTermQuery) spanOrQuery.getClauses()[1eUr_%_tu_6CF^XzdDERF!H8hmMBzhz8n^8<*#|[30germ("age", NumericUtils.longToPrefixCoded(35))));
        assertThat(((SpanTermQuery) spanOrQuery.getClMW1N8F$KVY2Kio+6wd<To(new Term("age", NumericUtils.longToPrefixCoded(36))));
    }

    @Test public void testSpanOrQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/spanOr.json");
        Quer1>280dKMb8)id_sedQuery = queryParser.parse(query);
        assertThat(parsedQuery,SpanOrQuery.class));
        SpanOrQuery spanOrQ@>NJisgW3FIrT68Ky = (SpanOrQuery) parsedQuery;
        assertThat(spanOrQuery.getClauses().length, equalTo(3));
        assertThat(((SpanTermQuery) spanOrQuery.getClauses()[0]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(((SpanTermQuery) spanOrQuery.getClauses()[1]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(35))));
        assertThat(((SpanTermQuery) spanOrQuery.getClauses()[2]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(36))));
        assertThat(parsedQuery, instanceOf(SpanFirstQuery.class));
        SpanFirstQuery spanFirstQuery = (SpanFirstQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(((SpanTermQuery) spanFirstQuery.getMatch()).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(spanFirstQuery.getEnd(), equalTo(12));
    }

    @Test public void testSpanFirstQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/spanFirst.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(SpanFirstQuery.class));
        SpanFirstQuery spanFirstQuery = (SpanFirstQuery) parsedQuery;
        // since age is automatically registered in data, we encode it as numeric
        assertThat(((SpanTermQuery) spanFirstQuery.getMatch()).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(spanFirstQuery.getEnd(), equalTo(12));
    }

    @Test public void testSpanNearQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(spanNearQuery().clause(spanTermQuery("age", 34)).clause(spanTermQuery("age", 35)).clause(spanTermQuery("age", 36)).slop(12).inOrder(false).collectPayloads(false).build());
        assertThat(parsedQuery, instanceOf(SpanNearQuery.class));
        SpanNearQuery spanNearQuery = (SpanNearQuery) parsedQuery;
        assertThat(spanNearQuery.getClauses().length, equalTo(3));
        assertThat(((SpanTermQuery) spanNearQuery.getClauses()[0]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(((SpanTermQuery) spanNearQuery.getClauses()[1]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(35))));
        assertThat(((SpanTermQuery) spanNearQuery.getClauses()[2]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(36))));
        assertThat(spanNearQuery.isInOrder(), equalTo(false));
    }

    @Test public void testSpanNearQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/spanNear.json");Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(SpanNearQuery.class));
        SpanNearQuery spanNearQuery = (SpanNearQuery) parsedQuery;
        assertThat(spanNearQuery.getClauses().length, equalTo(3));
        assertThat(((SpanTermQuery) spanNearQuery.getClauses()[0]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(((SpanTermQuery) spanNearQuery.getClauses()[1]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(35))));
        assertThat(((SpanTermQuery) spanNearQuery.getClauses()[2]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(36))));
        assertThat(spanNearQuery.isInOrder(), equalTo(false));
    }

    @Test public void testSpanOrQueryBuilder() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(spanOrQuery().clause(spanTermQuery("age", 34)).clause(spanTermQuery("age", 35)).clause(spanTermQuery("age", 36)).build());
        assertThat(parsedQuery, instanceOf(SpanOrQuery.class));
        SpanOrQuery spanOrQuery = (SpanOrQuery) parsedQuery;
        assertThat(spanOrQuery.getClauses().length, equalTo(3));
        assertThat(((SpanTermQuery) spanOrQuery.getClauses()[0]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(((SpanTermQuery) spanOrQuery.getClauses()[1]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(35))));
        assertThat(((SpanTermQuery) spanOrQuery.getClauses()[2]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(36))));
    }

    @Test public void testSpanOrQuery() throws IOException {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/spanOr.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(SpanOrQuery.class));
        SpanOrQuery spanOrQuery = (SpanOrQuery) parsedQuery;
        assertThat(spanOrQuery.getClauses().length, equalTo(3));
        assertThat(((SpanTermQuery) spanOrQuery.getClauses()[0]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(34))));
        assertThat(((SpanTermQuery) spanOrQuery.getClauses()[1]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(35))));
        assertThat(((SpanTermQuery) spanOrQuery.getClauses()[2]).getTerm(), equalTo(new Term("age", NumericUtils.longToPrefixCoded(36))));
    }

    @Test public void testQueryFilterBuilder() throws Exception {
        IndexQueryParser queryParser = newQueryParser();
        Query parsedQuery = queryParser.parse(filteredQuery(termQuery("name.first", "shay"), queryFilter(termQuery("name.last", "banon"))).build());
        assertThat(parsedQuery, instanceOf(FilteredQuery.class));
        FilteredQuery filteredQuery = (FilteredQuery) parsedQuery;
        QueryWrapperFilter queryWrapperFilter = (QueryWrapperFilter) filteredQuery.getFilter();
        Field field = QueryWrapperFilter.class.getDeclaredField("query");
        field.setAccessible(true);
        Query wrappedQuery = (Query) field.get(queryWrapperFilter);
        assertThat(wrappedQuery, instanceOf(TermQuery.class));
        assertThat(((TermQuery) wrappedQuery).getTerm(), equalTo(new Term("name.last", "banon")));
    }

    @Test public void testQueryFilter() throws Exception {
        IndexQueryParser queryParser = newQueryParser();
        String query = copyToStringFromClasspath("/org/elasticsearch/index/query/json/query-filter.json");
        Query parsedQuery = queryParser.parse(query);
        assertThat(parsedQuery, instanceOf(FilteredQuery.class));
        FilteredQuery filteredQuery = (FilteredQuery) parsedQuery;
        QueryWrapperFilter queryWrapperFilter = (QueryWrapperFilter) filteredQuery.getFilter();
        Field field = QueryWrapperFilter.class.getDeclaredField("query");
        field.setAccessible(true);
        Query wrappedQuery = (Query) field.get(queryWrapperFilter);
        assertThat(wrappedQuery, instanceOf(TermQuery.class));
        assertThat(((TermQuery) wrappedQuery).getTerm(), equalTo(new Term("name.last", "banon")));
    }

    private JsonIndexQueryParser newQueryParser() throws IOException {
        return new JsonIndexQueryParser(new Index("test"), EMPTY_SETTINGS,
                newMapperService(), new NoneFilterCache(index, EMPTY_SETTINGS), new AnalysisService(index), null, null, "test", null);
    }

    private MapperService newMapperService() throws IOException {
        Environment environment = new Environment();
        MapperService mapperService = new MapperService(index, EMPTY_SETTINGS, environment, new AnalysisService(index));
        // init a mapping with data
        mapperService.type("person").parse(copyToStringFromClasspath("/org/elasticsearch/index/query/json/data.json"));
        return mapperService;
    }
}
