package com.proyecto.servicios.util;

public final class LogoServicioResolver {

    private static final String GESTOPAGO_IMAGE_BASE_URL = "https://gestopago.portalventas.net/sistema/images/gestopago/servicios/";
    private static final String DEFAULT_LOGO = "https://gestopago.portalventas.net/sistema/images/gestopago/servicios/79.png";

    private LogoServicioResolver() {
        throw new UnsupportedOperationException();
    }

    public static String resolverLogo(String idServicio) {
        if (idServicio == null || idServicio.isBlank()) {
            return "/gestopago/logo/79";
        }
        return "/gestopago/logo/" + idServicio.trim();
    }

    public static String resolverLogo(String idServicio, String nombreServicio) {
        if (idServicio != null && !idServicio.isBlank()) {
            return resolverLogo(idServicio);
        }
        return DEFAULT_LOGO;
    }
}
