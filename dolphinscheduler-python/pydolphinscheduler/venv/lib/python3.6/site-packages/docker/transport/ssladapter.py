""" Resolves OpenSSL issues in some servers:
      https://lukasa.co.uk/2013/01/Choosing_SSL_Version_In_Requests/
      https://github.com/kennethreitz/requests/pull/799
"""
import sys

from distutils.version import StrictVersion
from requests.adapters import HTTPAdapter

from docker.transport.basehttpadapter import BaseHTTPAdapter

try:
    import requests.packages.urllib3 as urllib3
except ImportError:
    import urllib3


PoolManager = urllib3.poolmanager.PoolManager

# Monkey-patching match_hostname with a version that supports
# IP-address checking. Not necessary for Python 3.5 and above
if sys.version_info[0] < 3 or sys.version_info[1] < 5:
    from backports.ssl_match_hostname import match_hostname
    urllib3.connection.match_hostname = match_hostname


class SSLHTTPAdapter(BaseHTTPAdapter):
    '''An HTTPS Transport Adapter that uses an arbitrary SSL version.'''

    __attrs__ = HTTPAdapter.__attrs__ + ['assert_fingerprint',
                                         'assert_hostname',
                                         'ssl_version']

    def __init__(self, ssl_version=None, assert_hostname=None,
                 assert_fingerprint=None, **kwargs):
        self.ssl_version = ssl_version
        self.assert_hostname = assert_hostname
        self.assert_fingerprint = assert_fingerprint
        super().__init__(**kwargs)

    def init_poolmanager(self, connections, maxsize, block=False):
        kwargs = {
            'num_pools': connections,
            'maxsize': maxsize,
            'block': block,
            'assert_hostname': self.assert_hostname,
            'assert_fingerprint': self.assert_fingerprint,
        }
        if self.ssl_version and self.can_override_ssl_version():
            kwargs['ssl_version'] = self.ssl_version

        self.poolmanager = PoolManager(**kwargs)

    def get_connection(self, *args, **kwargs):
        """
        Ensure assert_hostname is set correctly on our pool

        We already take care of a normal poolmanager via init_poolmanager

        But we still need to take care of when there is a proxy poolmanager
        """
        conn = super().get_connection(*args, **kwargs)
        if conn.assert_hostname != self.assert_hostname:
            conn.assert_hostname = self.assert_hostname
        return conn

    def can_override_ssl_version(self):
        urllib_ver = urllib3.__version__.split('-')[0]
        if urllib_ver is None:
            return False
        if urllib_ver == 'dev':
            return True
        return StrictVersion(urllib_ver) > StrictVersion('1.5')
