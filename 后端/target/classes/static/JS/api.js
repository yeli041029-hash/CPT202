(function (window) {
  function getErrorMessage(error) {
    if (!error) {
      return 'Request failed.';
    }
    if (typeof error === 'string') {
      return error;
    }
    if (error.data && typeof error.data === 'string') {
      return error.data;
    }
    if (error.data && error.data.message) {
      return error.data.message;
    }
    if (error.data && error.data.msg) {
      return error.data.msg;
    }
    if (error.message) {
      return error.message;
    }
    return 'Request failed.';
  }

  async function request(path, options) {
    const config = Object.assign({ method: 'GET' }, options || {});
    const headers = Object.assign({}, config.headers || {});

    if (config.body && typeof config.body !== 'string') {
      headers['Content-Type'] = 'application/json';
      config.body = JSON.stringify(config.body);
    }

    config.headers = headers;

    const response = await fetch(path, config);
    const text = await response.text();
    let data = null;

    if (text) {
      try {
        data = JSON.parse(text);
      } catch (error) {
        data = text;
      }
    }

    if (!response.ok) {
      const requestError = new Error(getErrorMessage({ message: response.statusText, data: data }));
      requestError.status = response.status;
      requestError.data = data;
      throw requestError;
    }

    return data;
  }

  function unwrapResult(result) {
    if (!result) {
      return null;
    }
    if (typeof result.code === 'number' && result.code !== 200) {
      const requestError = new Error(result.msg || 'Request failed.');
      requestError.data = result;
      throw requestError;
    }
    return Object.prototype.hasOwnProperty.call(result, 'data') ? result.data : result;
  }

  function formatDateTime(value) {
    if (!value) {
      return 'N/A';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return String(value);
    }
    return date.toLocaleString();
  }

  function fallbackImage(label) {
    const text = String(label || 'Heritage')
      .slice(0, 24)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
    return 'data:image/svg+xml;utf8,' + encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 360">' +
      '<rect width="640" height="360" fill="#e8dcc1"/>' +
      '<rect x="30" y="30" width="580" height="300" rx="24" fill="#f7f1e2" stroke="#b68d4c" stroke-width="4"/>' +
      '<text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-size="28" fill="#6a4e2a" font-family="Georgia, serif">' + text + '</text>' +
      '</svg>'
    );
  }

  window.HeritageApi = {
    request: request,
    unwrapResult: unwrapResult,
    formatDateTime: formatDateTime,
    fallbackImage: fallbackImage,
    getErrorMessage: getErrorMessage
  };
})(window);
