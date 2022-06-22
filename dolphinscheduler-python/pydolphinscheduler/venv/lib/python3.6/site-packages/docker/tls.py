import os
import ssl

from . import errors
from .transport import SSLHTTPAdapter


class TLSConfig:
    """
    TLS configuration.

    Args:
        client_cert (tuple of str): Path to client cert, path to client key.
        ca_cert (str): Path to CA cert file.
        verify (bool or str): This can be ``False`` or a path to a CA cert
            file.
        ssl_version (int): A valid `SSL version`_.
        assert_hostname (bool): Verify the hostname of the server.

    .. _`SSL version`:
        https://docs.python.org/3.5/library/ssl.html#ssl.PROTOCOL_TLSv1
    """
    cert = None
    ca_cert = None
    verify = None
    ssl_version = None

    def __init__(self, client_cert=None, ca_cert=None, verify=None,
                 ssl_version=None, assert_hostname=None,
                 assert_fingerprint=None):
        # Argument compatibility/mapping with
        # https://docs.docker.com/engine/articles/https/
        # This diverges from the Docker CLI in that users can specify 'tls'
        # here, but also disable any public/default CA pool verification by
        # leaving verify=False

        self.assert_hostname = assert_hostname
        self.assert_fingerprint = assert_fingerprint

        # TODO(dperny): according to the python docs, PROTOCOL_TLSvWhatever is
        # depcreated, and it's recommended to use OPT_NO_TLSvWhatever instead
        # to exclude versions. But I think that might require a bigger
        # architectural change, so I've opted not to pursue it at this time

        # If the user provides an SSL version, we should use their preference
        if ssl_version:
            self.ssl_version = ssl_version
        else:
            # If the user provides no ssl version, we should default to
            # TLSv1_2.  This option is the most secure, and will work for the
            # majority of users with reasonably up-to-date software. However,
            # before doing so, detect openssl version to ensure we can support
            # it.
            if ssl.OPENSSL_VERSION_INFO[:3] >= (1, 0, 1) and hasattr(
                    ssl, 'PROTOCOL_TLSv1_2'):
                # If the OpenSSL version is high enough to support TLSv1_2,
                # then we should use it.
                self.ssl_version = getattr(ssl, 'PROTOCOL_TLSv1_2')
            else:
                # Otherwise, TLS v1.0 seems to be the safest default;
                # SSLv23 fails in mysterious ways:
                # https://github.com/docker/docker-py/issues/963
                self.ssl_version = ssl.PROTOCOL_TLSv1

        # "client_cert" must have both or neither cert/key files. In
        # either case, Alert the user when both are expected, but any are
        # missing.

        if client_cert:
            try:
                tls_cert, tls_key = client_cert
            except ValueError:
                raise errors.TLSParameterError(
                    'client_cert must be a tuple of'
                    ' (client certificate, key file)'
                )

            if not (tls_cert and tls_key) or (not os.path.isfile(tls_cert) or
                                              not os.path.isfile(tls_key)):
                raise errors.TLSParameterError(
                    'Path to a certificate and key files must be provided'
                    ' through the client_cert param'
                )
            self.cert = (tls_cert, tls_key)

        # If verify is set, make sure the cert exists
        self.verify = verify
        self.ca_cert = ca_cert
        if self.verify and self.ca_cert and not os.path.isfile(self.ca_cert):
            raise errors.TLSParameterError(
                'Invalid CA certificate provided for `ca_cert`.'
            )

    def configure_client(self, client):
        """
        Configure a client with these TLS options.
        """
        client.ssl_version = self.ssl_version

        if self.verify and self.ca_cert:
            client.verify = self.ca_cert
        else:
            client.verify = self.verify

        if self.cert:
            client.cert = self.cert

        client.mount('https://', SSLHTTPAdapter(
            ssl_version=self.ssl_version,
            assert_hostname=self.assert_hostname,
            assert_fingerprint=self.assert_fingerprint,
        ))
