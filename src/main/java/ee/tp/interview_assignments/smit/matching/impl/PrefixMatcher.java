package ee.tp.interview_assignments.smit.matching.impl;

import ee.tp.interview_assignments.smit.camel_case.CamelCaseTokenizer;
import ee.tp.interview_assignments.smit.matching.ClassNameMatcher;
import ee.tp.interview_assignments.smit.names.JavaClassName;
import ee.tp.interview_assignments.smit.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrefixMatcher implements ClassNameMatcher {
	private final List<QueryToken> queryTokens;
	private final boolean matchLastTokenExactly;

	protected static class QueryToken {
		String token;
		boolean containsWildcard;

		QueryToken(String token) {
			this.token = token;
			this.containsWildcard = token.indexOf(WILDCARD) >= 0;
		}
	}

	public PrefixMatcher(String query, boolean matchLastTokenExactly) {
		List<String> queryTokens = new CamelCaseTokenizer(query).getTokenList();
		this.queryTokens = queryTokens.stream()
				.map(QueryToken::new)
				.collect(Collectors.toCollection(() -> new ArrayList<>(queryTokens.size())));
		this.matchLastTokenExactly = matchLastTokenExactly;
	}

	protected boolean tokenMatch(String token, QueryToken queryToken) {
		if (!queryToken.containsWildcard)
			return token.startsWith(queryToken.token);

		return StringUtils.startsWith(token, queryToken.token, WILDCARD);
	}

	@Override
	public boolean matches(JavaClassName name) {
		String simpleName = name.getSimpleName();
		List<String> nameTokens = new CamelCaseTokenizer(simpleName).getTokenList();

		for (int i = 0, q = 0; i < nameTokens.size(); i++) {
			QueryToken queryToken = queryTokens.get(q);

			if (matchLastTokenExactly && q == queryTokens.size() - 1)
				return queryToken.token.equals(nameTokens.get(nameTokens.size() - 1));

			if (tokenMatch(nameTokens.get(i), queryToken)) {
				if ((++q) == queryTokens.size())
					return true;
			}
		}

		return false;
	}
}
