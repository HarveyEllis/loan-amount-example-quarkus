/* (C)2021 */
package com.example.integration;

import io.quarkus.test.junit.QuarkusTestProfile;

public class IntegrationProfile implements QuarkusTestProfile {

    @Override
    public String getConfigProfile() {
        return "prod";
    }
}
