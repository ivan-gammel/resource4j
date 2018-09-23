package com.github.resource4j.i18n.plural_rules.ldml;

import com.github.resource4j.i18n.plural_rules.PluralCase;
import com.github.resource4j.i18n.plural_rules.PluralCategory;
import com.github.resource4j.i18n.plural_rules.PluralRule;
import com.github.resource4j.i18n.plural_rules.PluralRuleBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.util.*;

import static com.github.resource4j.i18n.plural_rules.GenericPluralCase.aCase;
import static com.github.resource4j.i18n.plural_rules.PluralRuleBuilder.aRuleFor;

public final class LDMLRuleLoader {

    public static Map<Locale, PluralRule> load() {
        try {
            Map<Locale, PluralRule> index = new HashMap<>();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    if (systemId.endsWith("ldmlSupplemental.dtd")) {
                        return new InputSource(getClass().getResourceAsStream("/cldr/common/dtd/ldmlSupplemental.dtd"));
                    }
                    return null;
                }
            });
            Document document = builder.parse(LDMLRuleLoader.class.getResourceAsStream("/cldr/common/supplemental/plurals.xml"));
            NodeList ruleList = document.getElementsByTagName("pluralRules");
            for (int i = 0; i < ruleList.getLength(); i++) {
                Element rules = (Element) ruleList.item(i);
                String[] locales = rules.getAttribute("locales").split(" ");
                NodeList ruleDefs = rules.getElementsByTagName("pluralRule");
                List<PluralCase> cases = new ArrayList<>();
                for (int j = 0; j < ruleDefs.getLength(); j++) {
                    Element rule = (Element) ruleDefs.item(j);
                    String count = rule.getAttribute("count");
                    String text = rule.getTextContent();
                    int idx = text.indexOf("@integer");
                    if (idx < 0) idx = text.indexOf("@decimal");
                    if (idx > 0) {
                        text = text.substring(0, idx).trim();
                    }
                    cases.add(aCase(text.isEmpty() ? (number -> true) : LDMLPredicateParser.parse(text), PluralCategory.valueOf(count)));
                }
                for (String locale : locales) {
                    PluralRuleBuilder ruleBuilder = aRuleFor(Locale.forLanguageTag(locale));
                    for (PluralCase pluralCase : cases) {
                        ruleBuilder.add(pluralCase);
                    }
                    PluralRule rule = ruleBuilder.build();
                    index.put(rule.language(), rule);
                }
            }
            return index;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load pluralization rules", e);
        }
    }

}
