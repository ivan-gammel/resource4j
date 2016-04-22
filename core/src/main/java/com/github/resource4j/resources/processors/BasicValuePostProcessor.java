package com.github.resource4j.resources.processors;

import com.github.resource4j.ResourceException;

public class BasicValuePostProcessor implements ResourceValuePostProcessor {

    @Override
    public String process(ResourceResolver resolver, String value) throws ResourceException {
        StringBuilder result = new StringBuilder();
        StringBuilder current = result;
        boolean escape = false;
        boolean macro = false;
        boolean partialResult = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (escape) {
                if (c != '{' && c != '}' && c != '\\') {
                    current.append('\\');
                }
                escape = false;
                current.append(c);
                if (macro && c == '}') {
                    macro = false;
                    result.append('{').append(current);
                    current = result;
                    partialResult = true;
                }
            } else {
                switch (c) {
                    case '{':
                        if (macro) {
                            result.append('{');
                            result.append(current);
                            partialResult = true;
                        } else {
                            macro = true;
                        }
                        current = new StringBuilder();
                        break;
                    case '}':
                        if (macro) {
                            String substitute = resolver.get(current.toString());
                            if (substitute != null) {
                                result.append(substitute);
                            } else {
                                result.append('{').append(current.toString()).append('}');
                                partialResult = true;
                            }
                            current = result;
                            macro = false;
                        } else {
                            partialResult = true;
                            current.append(c);
                        }
                        break;
                    case '\\':
                        escape = true;
                        break;
                    default:
                        current.append(c);
                }
            }
        }
        if (current != result) {
            partialResult = true;
            if (macro) {
                result.append('{');
            }
            result.append(current);
        }
        if (escape) {
            partialResult = true;
            result.append('\\');
        }
        if (partialResult) {
            throw new ValuePostProcessingException(result.toString());
        }
        return result.toString();
    }

}
