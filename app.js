// ==========================================================================
// TEXTHACK CLIENT LOGIC & API INTERACTION
// ==========================================================================

document.addEventListener('DOMContentLoaded', () => {
  initNavigation();
  initDynamicInputs();
  initActionHandlers();
  loadInitialSystemStatus();
});

// ==========================================================================
// NAVIGATION SYSTEM
// ==========================================================================
function initNavigation() {
  const tabs = document.querySelectorAll('.nav-tab');
  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const targetId = tab.getAttribute('data-tab');
      switchTab(targetId);
    });
  });
}

function switchTab(tabId) {
  document.querySelectorAll('.nav-tab').forEach(t => {
    t.classList.toggle('active', t.getAttribute('data-tab') === tabId);
  });
  document.querySelectorAll('.tab-pane').forEach(p => {
    p.classList.toggle('active', p.id === tabId);
  });
}

// ==========================================================================
// DYNAMIC FORM VISIBILITY
// ==========================================================================
function initDynamicInputs() {
  // String Algorithm Select Change
  const strSelect = document.getElementById('string-algo-select');
  const patMinerOpts = document.getElementById('pattern-miner-opts');
  const minLenSlider = document.getElementById('min-len-slider');
  const minLenVal = document.getElementById('min-len-val');

  if (minLenSlider) {
    minLenSlider.addEventListener('input', () => {
      minLenVal.innerText = minLenSlider.value;
    });
  }

  strSelect.addEventListener('change', () => {
    patMinerOpts.style.display = (strSelect.value === 'patternminer') ? 'block' : 'none';
  });

  // NP Algorithm Select Change
  const npSelect = document.getElementById('np-algo-select');
  const fptasOpts = document.getElementById('fptas-opts');
  const fptasSlider = document.getElementById('fptas-eps-slider');
  const fptasVal = document.getElementById('fptas-eps-val');

  if (fptasSlider) {
    fptasSlider.addEventListener('input', () => {
      fptasVal.innerText = fptasSlider.value;
    });
  }

  npSelect.addEventListener('change', () => {
    fptasOpts.style.display = (npSelect.value === 'knapsackfptas') ? 'block' : 'none';
  });

  // Random/Parallel Select Change
  const rpSelect = document.getElementById('rp-algo-select');
  const mrOpt = document.getElementById('mr-number-opt');

  rpSelect.addEventListener('change', () => {
    mrOpt.style.display = (rpSelect.value === 'millerrabin') ? 'block' : 'none';
  });
}

// ==========================================================================
// INITIAL DATA LOADING
// ==========================================================================
async function loadInitialSystemStatus() {
  try {
    const res = await fetch('/api/status');
    const data = await res.json();
    if (data.status) {
      document.getElementById('jvm-status').innerText = `Online (${data.jvm} / ${data.availableCores} Cores)`;
      document.getElementById('corpus-stat-size').innerText = `${(data.corpusLength / 1024).toFixed(1)} KB`;
    }
  } catch (err) {
    console.warn('Backend not responding to /api/status:', err);
  }

  try {
    const res = await fetch('/api/corpus');
    const data = await res.json();
    if (data.preview) {
      document.getElementById('corpus-preview-box').innerText = data.preview + '\n... [Total: ' + data.totalCharacters + ' characters]';
    }
  } catch (err) {
    console.warn('Backend not responding to /api/corpus:', err);
  }
}

// ==========================================================================
// API ACTION HANDLERS
// ==========================================================================
function initActionHandlers() {
  // 1. String Algorithms (M1 & M2)
  document.getElementById('btn-run-string-search').addEventListener('click', async () => {
    const algo = document.getElementById('string-algo-select').value;
    const pattern = document.getElementById('string-pattern-input').value;
    const customText = document.getElementById('string-custom-text').value;
    const minLength = document.getElementById('min-len-slider').value;

    const out = document.getElementById('string-results-content');
    const badge = document.getElementById('string-exec-time');
    out.innerHTML = '<p class="placeholder-text">Running ' + algo + '...</p>';
    badge.innerText = 'Executing...';

    try {
      const res = await fetch('/api/search', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ algorithm: algo, pattern, text: customText, minLength })
      });
      const json = await res.json();
      badge.innerText = `${json.timeMs.toFixed(3)} ms`;

      if (algo === 'suffixarray') {
        renderSuffixArrayTable(json.data.table, out);
      } else if (algo === 'patternminer') {
        renderPatternMinerResults(json.data.repeats, out);
      } else if (algo === 'ahocorasick') {
        renderAhoCorasickResults(json.data.matches, out);
      } else {
        renderExactSearchResults(json.data, out);
      }
    } catch (err) {
      out.innerHTML = `<div class="metric-box"><div class="metric-title" style="color:var(--accent-rose);">Error</div><div class="metric-value">${err.message}</div></div>`;
      badge.innerText = 'Failed';
    }
  });

  // 2. Advanced DP (M3)
  document.getElementById('btn-run-dp').addEventListener('click', async () => {
    const type = document.getElementById('dp-algo-select').value;
    const s1 = document.getElementById('dp-s1').value;
    const s2 = document.getElementById('dp-s2').value;

    const out = document.getElementById('dp-results-content');
    const badge = document.getElementById('dp-exec-time');
    out.innerHTML = '<p class="placeholder-text">Computing DP table & traceback...</p>';
    badge.innerText = 'Computing...';

    try {
      const res = await fetch('/api/dp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type, s1, s2 })
      });
      const json = await res.json();
      badge.innerText = 'Completed';
      renderDPResults(type, json.data, out);
    } catch (err) {
      out.innerHTML = `<div class="metric-box"><div class="metric-title" style="color:var(--accent-rose);">Error</div><div class="metric-value">${err.message}</div></div>`;
      badge.innerText = 'Failed';
    }
  });

  // 3. Network Flow (M4)
  document.getElementById('btn-run-flow').addEventListener('click', async () => {
    const type = document.getElementById('flow-algo-select').value;
    const out = document.getElementById('flow-results-content');
    const badge = document.getElementById('flow-exec-time');
    out.innerHTML = '<p class="placeholder-text">Solving residual flow network...</p>';
    badge.innerText = 'Solving...';

    try {
      const res = await fetch('/api/flow', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type })
      });
      const json = await res.json();
      badge.innerText = 'Completed';
      renderFlowResults(type, json.data, out);
    } catch (err) {
      out.innerHTML = `<div class="metric-box"><div class="metric-title" style="color:var(--accent-rose);">Error</div><div class="metric-value">${err.message}</div></div>`;
      badge.innerText = 'Failed';
    }
  });

  // 4. NP & Approximation (M5)
  document.getElementById('btn-run-np').addEventListener('click', async () => {
    const type = document.getElementById('np-algo-select').value;
    const eps = document.getElementById('fptas-eps-slider').value;
    const out = document.getElementById('np-results-content');
    const badge = document.getElementById('np-exec-time');
    out.innerHTML = '<p class="placeholder-text">Running NP verifier / approximation...</p>';
    badge.innerText = 'Computing...';

    try {
      const res = await fetch('/api/np', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type, epsilon: eps })
      });
      const json = await res.json();
      badge.innerText = 'Completed';
      renderNPResults(type, json.data, out);
    } catch (err) {
      out.innerHTML = `<div class="metric-box"><div class="metric-title" style="color:var(--accent-rose);">Error</div><div class="metric-value">${err.message}</div></div>`;
      badge.innerText = 'Failed';
    }
  });

  // 5. Randomised & Parallel (M6)
  document.getElementById('btn-run-rp').addEventListener('click', async () => {
    const type = document.getElementById('rp-algo-select').value;
    const number = document.getElementById('mr-number-input').value;
    const out = document.getElementById('rp-results-content');
    const badge = document.getElementById('rp-exec-time');
    out.innerHTML = '<p class="placeholder-text">Executing randomised / parallel primitive...</p>';
    badge.innerText = 'Executing...';

    try {
      const res = await fetch('/api/random-parallel', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type, number })
      });
      const json = await res.json();
      badge.innerText = 'Completed';
      renderRandomParallelResults(type, json.data, out);
    } catch (err) {
      out.innerHTML = `<div class="metric-box"><div class="metric-title" style="color:var(--accent-rose);">Error</div><div class="metric-value">${err.message}</div></div>`;
      badge.innerText = 'Failed';
    }
  });
}

// ==========================================================================
// RENDERERS FOR RESULT VIEWS
// ==========================================================================

function renderExactSearchResults(data, container) {
  let html = `
    <div class="metric-box">
      <div class="metric-title">Algorithm: ${data.algorithm}</div>
      <div class="metric-value">${data.matchCount} Matches Found</div>
    </div>
    <div class="metric-box">
      <div class="metric-title">Match Starting Indices (0-indexed):</div>
      <div class="metric-value">${JSON.stringify(data.matches)}</div>
    </div>
  `;
  if (data.lps) {
    html += `
      <div class="metric-box">
        <div class="metric-title">KMP Failure Function (LPS Array):</div>
        <div class="metric-value">${JSON.stringify(data.lps)}</div>
      </div>
    `;
  }
  if (data.zArray) {
    html += `
      <div class="metric-box">
        <div class="metric-title">Z-Algorithm Window Table (Z-box Values):</div>
        <div class="metric-value">${JSON.stringify(data.zArray)}</div>
      </div>
    `;
  }
  container.innerHTML = html;
}

function renderAhoCorasickResults(matches, container) {
  let html = `<div class="metric-box"><div class="metric-title">Aho-Corasick Multi-Pattern Results:</div></div>`;
  html += `<table class="data-table"><thead><tr><th>Keyword Pattern</th><th>Occurrences</th><th>Corpus Positions</th></tr></thead><tbody>`;
  for (const [kw, pos] of Object.entries(matches)) {
    html += `<tr><td><strong>${escapeHtml(kw)}</strong></td><td>${pos.length}</td><td>${JSON.stringify(pos)}</td></tr>`;
  }
  html += `</tbody></table>`;
  container.innerHTML = html;
}

function renderSuffixArrayTable(table, container) {
  let html = `
    <div class="metric-box">
      <div class="metric-title">Suffix Array (Prefix Doubling) & Kasai LCP Array</div>
      <div class="metric-value">Showing top ${table.length} sorted suffixes</div>
    </div>
    <div class="table-scroll">
      <table class="data-table">
        <thead>
          <tr>
            <th>Rank</th>
            <th>SA[i] (Suffix Index)</th>
            <th>LCP[i]</th>
            <th>Suffix Preview</th>
          </tr>
        </thead>
        <tbody>
  `;
  table.forEach(r => {
    html += `
      <tr>
        <td>${r.rank}</td>
        <td><strong>${r.sa}</strong></td>
        <td><span style="color:var(--accent-cyan); font-weight:700;">${r.lcp}</span></td>
        <td><code>${escapeHtml(r.suffix)}</code></td>
      </tr>
    `;
  });
  html += `</tbody></table></div>`;
  container.innerHTML = html;
}

function renderPatternMinerResults(repeats, container) {
  if (!repeats || repeats.length === 0) {
    container.innerHTML = `<div class="metric-box"><div class="metric-title">No Repeated Patterns Found</div><div class="metric-value">Try lowering the minimum length slider.</div></div>`;
    return;
  }
  let html = `
    <div class="metric-box">
      <div class="metric-title">Repeated Pattern Mining (via LCP Interval Stack)</div>
      <div class="metric-value">${repeats.length} distinct repeated patterns discovered</div>
    </div>
    <div class="table-scroll">
      <table class="data-table">
        <thead>
          <tr>
            <th>Pattern Substring</th>
            <th>Length</th>
            <th>Occurrences</th>
            <th>Corpus Positions</th>
          </tr>
        </thead>
        <tbody>
  `;
  repeats.forEach(p => {
    html += `
      <tr>
        <td><strong style="color:var(--accent-amber);">${escapeHtml(p.pattern)}</strong></td>
        <td>${p.length}</td>
        <td><span class="badge-status">${p.occurrences}x</span></td>
        <td><code>${JSON.stringify(p.positions)}</code></td>
      </tr>
    `;
  });
  html += `</tbody></table></div>`;
  container.innerHTML = html;
}

function renderDPResults(type, data, container) {
  if (type === 'levenshtein') {
    let html = `
      <div class="metric-box">
        <div class="metric-title">Wagner-Fischer Dynamic Programming</div>
        <div class="metric-value">Edit Distance between "${data.s1}" and "${data.s2}": <span style="color:var(--accent-cyan);">${data.distance}</span></div>
      </div>
      <div class="metric-title" style="margin-bottom:0.5rem;">2D Tabulation Matrix dp[i][j]:</div>
      <div class="table-scroll">
        <div class="matrix-grid">
    `;
    data.table.forEach((row, i) => {
      html += `<div class="matrix-row">`;
      row.forEach((val, j) => {
        const isHeader = (i === 0 || j === 0);
        html += `<div class="matrix-cell ${isHeader ? 'header' : ''}">${val}</div>`;
      });
      html += `</div>`;
    });
    html += `</div></div>`;
    container.innerHTML = html;
  } else if (type === 'needleman' || type === 'smithwaterman') {
    let html = `
      <div class="metric-box">
        <div class="metric-title">${type === 'needleman' ? 'Needleman-Wunsch Global Alignment' : 'Smith-Waterman Local Alignment'}</div>
        <div class="metric-value">Optimal Alignment Score: <span style="color:var(--accent-emerald);">${data.score}</span></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Optimal Traceback Alignment:</div>
        <div class="metric-value" style="letter-spacing: 0.15em;">
          Seq 1: <code>${data.alignedSeq1}</code><br>
          Seq 2: <code>${data.alignedSeq2}</code>
        </div>
      </div>
    `;
    container.innerHTML = html;
  } else if (type === 'matrixchain') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Matrix Chain Multiplication DP (O(n┬│))</div>
        <div class="metric-value">Minimum Scalar Multiplications: <span style="color:var(--accent-cyan); font-size:1.25rem;">${data.minOperations}</span></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Optimal Parenthesization:</div>
        <div class="metric-value"><code>${data.parenthesization}</code></div>
      </div>
    `;
  } else if (type === 'tsp') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Bitmask DP: Travelling Salesman Problem (O(2Γü┐ ┬╖ n┬▓))</div>
        <div class="metric-value">Optimal Hamiltonian Cycle Cost: <span style="color:var(--accent-emerald); font-size:1.25rem;">${data.minCost}</span></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Optimal Tour Path:</div>
        <div class="metric-value"><code>${data.tour.join(' &rarr; ')}</code></div>
      </div>
    `;
  } else if (type === 'treedp') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Tree Dynamic Programming</div>
        <div class="metric-value">Maximum Weight Independent Set (MWIS): <span style="color:var(--accent-cyan);">${data.mwis}</span></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Tree Diameter:</div>
        <div class="metric-value">${data.diameter} edges</div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Rerooting Technique: All-Roots Sum of Distances (O(n)):</div>
        <div class="metric-value"><code>${JSON.stringify(data.allRootsDistances)}</code></div>
      </div>
    `;
  } else if (type === 'sosdp') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">SOS DP: Sum Over Subsets (Yates's Algorithm - O(n ┬╖ 2Γü┐))</div>
        <div class="metric-value">Calculated subset-sum transform for feature inclusion-exclusion:</div>
      </div>
      <div class="metric-box">
        <div class="metric-title">SOS Result Array (Aggregated submask totals):</div>
        <div class="metric-value"><code>${JSON.stringify(data.sosResult)}</code></div>
      </div>
    `;
  } else if (type === 'diagnostic') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">DP Diagnostic Evaluation</div>
        <div class="metric-value">Suitable for DP: <span class="badge-status">${data.isDPSuitable ? 'YES' : 'NO'}</span></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Recommendation:</div>
        <div class="metric-value">${data.recommendation}</div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Theoretical Rationale:</div>
        <div class="metric-value" style="font-size:0.85rem; color:var(--text-muted);">${data.rationale}</div>
      </div>
    `;
  } else {
    container.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
  }
}

function renderFlowResults(type, data, container) {
  if (type === 'maxflow') {
    let html = `
      <div class="metric-box">
        <div class="metric-title">Dinic's Algorithm vs Ford-Fulkerson & Edmonds-Karp</div>
        <div class="metric-value">
          Dinic's Max Flow (O(V┬▓E)): <span style="color:var(--accent-cyan); font-weight:700;">${data.dinicMaxFlow}</span><br>
          Edmonds-Karp (O(VE┬▓)): ${data.edmondsKarp}<br>
          Ford-Fulkerson (DFS): ${data.fordFulkerson}
        </div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Max-Flow Min-Cut Theorem Duality:</div>
        <div class="metric-value">Min-Cut Edges Disconnecting S from T:</div>
      </div>
      <table class="data-table">
        <thead><tr><th>From (in S)</th><th>To (in T)</th><th>Cut Capacity</th></tr></thead>
        <tbody>
    `;
    data.minCutEdges.forEach(e => {
      html += `<tr><td>Node ${e.from}</td><td>Node ${e.to}</td><td><strong style="color:var(--accent-rose);">${e.capacity}</strong></td></tr>`;
    });
    html += `</tbody></table>`;
    container.innerHTML = html;
  } else if (type === 'bipartite') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Maximum Bipartite Matching & K├╢nig's Theorem</div>
        <div class="metric-value">Max Matching Size: <span style="color:var(--accent-emerald);">${data.maxMatchingSize}</span></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">K├╢nig's Theorem Verification:</div>
        <div class="metric-value">|Max Matching| (${data.maxMatchingSize}) = |Min Vertex Cover| (${data.minVertexCoverL.length + data.minVertexCoverR.length})</div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Matching Pairs:</div>
        <div class="metric-value">Left to Right: <code>${JSON.stringify(data.leftMatch)}</code></div>
      </div>
    `;
  } else if (type === 'mcmf') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Min-Cost Max-Flow (MCMF via Successive Shortest Path)</div>
        <div class="metric-value">Max Flow Pushed: <span style="color:var(--accent-cyan);">${data.maxFlow}</span></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Optimal Minimum Total Cost:</div>
        <div class="metric-value" style="color:var(--accent-emerald); font-size:1.25rem;">${data.minCost}</div>
      </div>
    `;
  } else if (type === 'projectselection') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Project Selection via Min-Cut (Maximum Weight Closure)</div>
        <div class="metric-value">Maximum Net Profit Achievable: <span style="color:var(--accent-emerald); font-size:1.25rem;">${data.maxNetProfit}</span></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Selected Projects (Source-Side Closure):</div>
        <div class="metric-value"><code>${JSON.stringify(data.selectedProjects)}</code></div>
      </div>
    `;
  } else if (type === 'scheduling') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Task Scheduling with Deadlines via Network Flow</div>
        <div class="metric-value">Feasible Schedule Exists: <span class="badge-status">${data.feasible ? 'YES' : 'NO'}</span></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Total Tasks Successfully Scheduled:</div>
        <div class="metric-value">${data.totalScheduledTasks} tasks</div>
      </div>
    `;
  } else {
    container.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
  }
}

function renderNPResults(type, data, container) {
  if (type === 'reduction') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Polynomial Reduction Pipeline: 3-SAT &rarr; Independent Set</div>
        <div class="metric-value">Formula reduced to graph gadget in polynomial time:</div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Gadget Properties:</div>
        <div class="metric-value">
          Total Gadget Vertices: <strong>${data.numVertices}</strong> (3 per clause)<br>
          Target Independent Set Size: <strong>${data.targetK}</strong><br>
          Conflict & Triangle Edges: <strong>${data.edgesCount}</strong>
        </div>
      </div>
    `;
  } else if (type === 'vertexcover2approx') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">2-Approximation for Minimum Vertex Cover via Maximal Matching</div>
        <div class="metric-value">Cover Size: <span style="color:var(--accent-cyan);">${data.coverSize}</span> (Guaranteed &le; 2.0 &times; OPT)</div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Selected Cover Vertices:</div>
        <div class="metric-value"><code>${JSON.stringify(data.cover)}</code></div>
      </div>
    `;
  } else if (type === 'metrictsp') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">2-Approximation for Metric TSP (MST Doubling + Shortcut DFS)</div>
        <div class="metric-value">Tour Cost: <span style="color:var(--accent-emerald);">${data.tourCost}</span> (Guaranteed &le; 2.0 &times; OPT)</div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Shortcut Tour Path:</div>
        <div class="metric-value"><code>${data.tour.join(' &rarr; ')}</code></div>
      </div>
    `;
  } else if (type === 'knapsackfptas') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Knapsack FPTAS (Fully Polynomial-Time Approx Scheme)</div>
        <div class="metric-value">Approximation Ratio: <span style="color:var(--accent-emerald);">&ge; ${(data.theoreticalLowerBoundRatio * 100).toFixed(0)}% of OPT</span> (&epsilon; = ${data.epsilon})</div>
      </div>
      <div class="metric-box">
        <div class="metric-title">FPTAS Solution:</div>
        <div class="metric-value">
          Total Value Obtained: <strong>${data.totalValue}</strong><br>
          Total Weight: <strong>${data.totalWeight}</strong> (within capacity)<br>
          Selected Item Indices: <code>${JSON.stringify(data.chosenItems)}</code>
        </div>
      </div>
    `;
  } else if (type === 'kernelization') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Parameterized Complexity: Buss's Rule Kernelization</div>
        <div class="metric-value">Mandatory High-Degree Vertices (deg &gt; k): <code>${JSON.stringify(data.mandatoryInCover)}</code></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Reduced Kernel Size:</div>
        <div class="metric-value">
          Reduced Parameter k': <strong>${data.reducedK}</strong><br>
          Remaining Kernel Edges: <strong>${data.kernelEdgesCount}</strong><br>
          Exact Bounded Search Tree FPT Cover (O(2ß╡Å ┬╖ n)): <code>${JSON.stringify(data.exactFPTCover)}</code>
        </div>
      </div>
    `;
  } else {
    container.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
  }
}

function renderRandomParallelResults(type, data, container) {
  if (type === 'qsort') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Las Vegas Randomised QuickSort</div>
        <div class="metric-value">Always Correct Output: <span class="badge-status">YES (Las Vegas)</span></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Execution Tracking:</div>
        <div class="metric-value">
          Comparisons Made: <strong>${data.comparisonCount}</strong> (Expected O(n log n))<br>
          Sorted Array: <code>${JSON.stringify(data.sorted)}</code>
        </div>
      </div>
    `;
  } else if (type === 'millerrabin') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Monte Carlo Miller-Rabin Primality Test</div>
        <div class="metric-value">Number Tested: <strong>${data.number}</strong> &rarr; <span class="badge-status" style="${data.isPrime ? '' : 'background:rgba(244,63,94,0.2); color:#fb7185; border-color:rgba(244,63,94,0.4);'}">${data.isPrime ? 'PRIME' : 'COMPOSITE'}</span></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Witness Verification Details:</div>
        <div class="metric-value" style="font-size:0.85rem; color:var(--text-muted);">${data.explanation}</div>
      </div>
    `;
  } else if (type === 'fks') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">2-Level Perfect Hashing (Fredman-Koml├│s-Szemer├⌐di / FKS)</div>
        <div class="metric-value">Worst-Case Lookup Time: <span style="color:var(--accent-emerald); font-weight:700;">${data.worstCaseLookupTime}</span></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Hash Architecture:</div>
        <div class="metric-value">
          Primary Buckets: <strong>${data.primaryBuckets}</strong><br>
          Secondary Buckets Allocation: <strong>Quadratic m_i = c_i┬▓</strong> (Guarantees 0 Collisions)<br>
          Indexed Keys: <code>${JSON.stringify(data.keys)}</code>
        </div>
      </div>
    `;
  } else if (type === 'reservoir') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Reservoir Sampling (Algorithm R) on Streaming Corpus</div>
        <div class="metric-value">Sample Size k: <strong>${data.sample.length}</strong> | Stream Items Seen: <strong>${data.elementsSeen}</strong></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Uniform Probability Proof:</div>
        <div class="metric-value">Each item has exact probability <code>k / N = ${(data.uniformProbability * 100).toFixed(2)}%</code> of appearing in sample.</div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Sampled Elements:</div>
        <div class="metric-value"><code>${JSON.stringify(data.sample)}</code></div>
      </div>
    `;
  } else if (type === 'blelloch') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Blelloch Work-Efficient Parallel Scan / Prefix-Sum</div>
        <div class="metric-value">Work: <span style="color:var(--accent-cyan);">${data.work}</span> | Span: <span style="color:var(--accent-emerald);">${data.span}</span></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Input Array:</div>
        <div class="metric-value"><code>${JSON.stringify(data.input)}</code></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Exclusive Prefix-Sum Result:</div>
        <div class="metric-value"><code>${JSON.stringify(data.exclusivePrefixSum)}</code></div>
      </div>
    `;
  } else if (type === 'parallelsearch') {
    container.innerHTML = `
      <div class="metric-box">
        <div class="metric-title">Data-Parallel Multi-Core Pattern Search</div>
        <div class="metric-value">Matches Found: <strong style="color:var(--accent-cyan);">${data.matchCount}</strong> | Active Cores/Chunks: <strong>${data.chunksUsed}</strong></div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Work & Span Metrics (Brent's Theorem):</div>
        <div class="metric-value">
          Total Work TΓéü: <strong>${data.workT1} ops</strong><br>
          Critical Path Span T_&infin;: <strong>${data.spanTInf} ops</strong><br>
          Brent's Theoretical Speedup Ceiling: <strong style="color:var(--accent-emerald);">${data.brentSpeedupCeiling.toFixed(2)}x</strong>
        </div>
      </div>
      <div class="metric-box">
        <div class="metric-title">Match Positions:</div>
        <div class="metric-value"><code>${JSON.stringify(data.matches)}</code></div>
      </div>
    `;
  } else {
    container.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
}
