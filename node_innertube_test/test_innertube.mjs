import { Innertube, UniversalCache, Log, Platform } from 'youtubei.js';
import { Jinter } from 'jintr';

Log.setLevel(Log.Level.ERROR);

// Correctly override the platform shim for evaluating YouTube's player scripts
Platform.shim.eval = (script, env) => {
  const code = typeof script === 'object' && script !== null && script.output ? script.output : script;
  const jinter = new Jinter(code);
  for (const [key, value] of Object.entries(env)) {
    jinter.scope.set(key, value);
  }
  
  // Run the code
  jinter.evaluate();
  
  // Return an object with the updated properties from the scope
  const result = {};
  for (const key of Object.keys(env)) {
     result[key] = jinter.scope.get(key);
  }
  return result;
};

async function testInnertube() {
  try {
    console.log('Initializing InnerTube...');
    
    // Initialize Innertube
    const youtube = await Innertube.create({
      cache: new UniversalCache(false),
      generate_session_locally: true
    });

    const videoIds = [
      { id: 'dj_zUJNv_zc', desc: 'Song: Finale by Bien' },
      { id: 'XqZsoesa55w', desc: 'Kids Video: Baby Shark Dance' },
      { id: 'jfKfPfyJRdk', desc: 'Live Stream: Lofi Girl' },
      { id: 'aqz-KE-bpKQ', desc: 'Short Film: Big Buck Bunny' }
    ];

    for (const { id, desc } of videoIds) {
      console.log(`\n================================`);
      console.log(`Testing: ${desc} (ID: ${id})`);
      console.log(`================================`);
      
      try {
        const info = await youtube.getInfo(id);
        console.log(`Title: ${info.basic_info.title}`);
        console.log(`Author: ${info.basic_info.author}`);
        console.log(`Is Live: ${info.basic_info.is_live}`);

        console.log('Getting stream link...');

        // For live streams, we might not have regular formats. 
        // We should just get the HLS manifest URL if available.
        if (info.basic_info.is_live && info.streaming_data?.hls_manifest_url) {
           console.log('--- Stream Link successfully retrieved (HLS Manifest) ---');
           console.log(info.streaming_data.hls_manifest_url);
           console.log('--- Test successful! ---');
           continue;
        }

        // Try getting video+audio best format
        let format;
        try {
          format = info.chooseFormat({ type: 'video+audio', quality: 'best' });
        } catch (e) {
          // Fallback if video+audio is not found
          try {
             format = info.chooseFormat({ type: 'video', quality: 'best' });
          } catch(e2) {
             if (info.streaming_data?.formats?.length > 0) {
                 format = info.streaming_data.formats[0];
             } else if (info.streaming_data?.adaptive_formats?.length > 0) {
                 format = info.streaming_data.adaptive_formats[0];
             }
          }
        }

        if (format) {
          // We can await the decipher process if it's a promise, but decipher is usually synchronous.
          // However, Platform.shim.eval might return a promise. 
          // `format.decipher(youtube.session.player)` might return a URL or a Promise.
          let url = format.url;
          if (!url && format.signature_cipher) {
             const deciphered = format.decipher(youtube.session.player);
             // handle if it's a promise
             url = deciphered instanceof Promise ? await deciphered : deciphered;
          }

          console.log('--- Stream Link successfully retrieved ---');
          console.log(url);
          console.log('Mime type:', format.mime_type);
          console.log('--- Test successful! ---');
        } else {
          console.error('Failed to retrieve stream link. No matching format found.');
        }
      } catch (err) {
        console.error(`Error processing video ${id}:`, err.message);
      }
    }
  } catch (error) {
    console.error('Error during InnerTube test:', error.message);
    if (error.stack) console.error(error.stack);
  }
}

testInnertube();
